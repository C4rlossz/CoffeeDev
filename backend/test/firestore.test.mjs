import {readFile} from 'node:fs/promises';
import {test,before,after,beforeEach} from 'node:test';
import {initializeTestEnvironment,assertFails,assertSucceeds} from '@firebase/rules-unit-testing';
import {doc,setDoc,getDoc,updateDoc,collection,getDocs,serverTimestamp} from 'firebase/firestore';
import {catalog} from '../domain.mjs';
let env;
before(async()=>{
  env=await initializeTestEnvironment({projectId:'demo-coffeedev',firestore:{rules:await readFile(new URL('../../firestore.rules',import.meta.url),'utf8')}});
});
after(async()=>env?.cleanup());
beforeEach(async()=>{
  await env.clearFirestore();
  await env.withSecurityRulesDisabled(async context=>{
    const db=context.firestore();
    await setDoc(doc(db,'system/owner'),{uid:'owner'});
    for(const [uid,status] of [['owner','active'],['customer','active'],['other','active'],['pending','pending'],['blocked','blocked']]) await setDoc(doc(db,`access/${uid}`),{uid,name:uid,email:`${uid}@example.com`,status});
    await setDoc(doc(db,'catalog/menu'),{products:Object.fromEntries(Object.entries(catalog).map(([id,priceCents])=>[id,{priceCents,available:true}]))});
    await setDoc(doc(db,'orders/private'),{uid:'customer',quantities:{espresso:1},totalCents:890,status:'received',createdAt:new Date(),note:''});
  });
});
const db=uid=>uid ? env.authenticatedContext(uid,{email:`${uid}@example.com`}).firestore():env.unauthenticatedContext().firestore();
const order=(extra={})=>({uid:'customer',quantities:{espresso:1},totalCents:890,status:'received',createdAt:serverTimestamp(),note:'',...extra});
test('visitors, pending and blocked accounts cannot read the menu',async()=>{
  for(const uid of [null,'pending','blocked']) await assertFails(getDoc(doc(db(uid),'catalog/menu')));
});
test('active account can read the menu',async()=>assertSucceeds(getDoc(doc(db('customer'),'catalog/menu'))));
test('new registration can only create its own pending access',async()=>{
  await assertSucceeds(setDoc(doc(db('new'),'access/new'),{uid:'new',name:'New User',email:'new@example.com',status:'pending'}));
  await assertFails(setDoc(doc(db('elevate'),'access/elevate'),{uid:'elevate',name:'New User',email:'elevate@example.com',status:'active'}));
});
test('account cannot approve itself or list other accounts',async()=>{
  await assertFails(updateDoc(doc(db('pending'),'access/pending'),{status:'active'}));
  await assertFails(getDocs(collection(db('customer'),'access')));
});
test('owner approves and suspends accounts but cannot change their identity',async()=>{
  await assertSucceeds(updateDoc(doc(db('owner'),'access/pending'),{status:'active'}));
  await assertSucceeds(updateDoc(doc(db('owner'),'access/customer'),{status:'blocked'}));
  await assertFails(updateDoc(doc(db('owner'),'access/customer'),{email:'other@example.com'}));
});
test('clients cannot replace the owner or suspend the owner',async()=>{
  await assertFails(setDoc(doc(db('customer'),'system/owner'),{uid:'customer'}));
  await assertFails(updateDoc(doc(db('owner'),'access/owner'),{status:'blocked'}));
});
test('active customer creates an order with the server catalog price',async()=>assertSucceeds(setDoc(doc(db('customer'),'orders/valid'),order())));
test('forged price, uid, status, timestamp, unknown product or excess quantity fails',async()=>{
  for(const extra of [{totalCents:1},{uid:'owner'},{status:'completed'},{createdAt:new Date(0)},{quantities:{unknown:1}},{quantities:{espresso:11},totalCents:9790},{quantities:{espresso:1.5},totalCents:1335},{quantities:{espresso:0},totalCents:0},{note:'x'.repeat(241)}]) await assertFails(setDoc(doc(db('customer'),'orders/forged'),order(extra)));
});
test('pending and blocked cannot create orders',async()=>{
  for(const uid of ['pending','blocked']) await assertFails(setDoc(doc(db(uid),'orders/no'),order({uid})));
});
test('unavailable product cannot be ordered',async()=>{
  await assertSucceeds(updateDoc(doc(db('owner'),'catalog/menu'),{'products.espresso.available':false}));
  await assertFails(setDoc(doc(db('customer'),'orders/no-stock'),order()));
});
test('customer cannot change prices, status, or read another customer order',async()=>{
  await assertFails(updateDoc(doc(db('customer'),'catalog/menu'),{'products.espresso.priceCents':100}));
  await assertFails(updateDoc(doc(db('customer'),'orders/private'),{status:'preparing'}));
  await assertFails(getDoc(doc(db('other'),'orders/private')));
});
test('owner follows ordered status transitions and cannot alter totals',async()=>{
  const ref=doc(db('owner'),'orders/private');
  await assertFails(updateDoc(ref,{status:'ready'}));
  await assertSucceeds(updateDoc(ref,{status:'preparing'}));
  await assertSucceeds(updateDoc(ref,{status:'ready'}));
  await assertSucceeds(updateDoc(ref,{status:'completed'}));
  await assertFails(updateDoc(ref,{totalCents:1}));
});

test('all ten products can be purchased in a single authorized request',async()=>{
  const quantities=Object.fromEntries(Object.keys(catalog).map(id=>[id,1]));
  const totalCents=Object.values(catalog).reduce((a,b)=>a+b,0);
  await assertSucceeds(setDoc(doc(db('customer'),'orders/full-menu'),order({quantities,totalCents})));
});
test('owner catalog updates retain complete typed schema',async()=>{
  const ref=doc(db('owner'),'catalog/menu');
  await assertSucceeds(updateDoc(ref,{'products.espresso.priceCents':990}));
  await assertFails(updateDoc(ref,{'products.espresso.priceCents':-1}));
  await assertFails(updateDoc(ref,{'products.croissant.available':'yes'}));
  await assertFails(updateDoc(ref,{'products.espresso.extra':'unrecognized'}));
  await assertFails(setDoc(ref,{products:{espresso:{priceCents:990,available:true}}}));
});
test('queries used by the app permit only scoped order listings',async()=>{
  const {query,where,orderBy,limit}=await import('firebase/firestore');
  await assertSucceeds(getDocs(query(collection(db('customer'),'orders'),where('uid','==','customer'),orderBy('createdAt','desc'),limit(100))));
  await assertSucceeds(getDocs(query(collection(db('owner'),'orders'),orderBy('createdAt','desc'),limit(100))));
  await assertFails(getDocs(collection(db('customer'),'orders')));
  await assertFails(getDocs(collection(db(null),'catalog')));
});
