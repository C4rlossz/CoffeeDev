import {initializeApp, applicationDefault} from 'firebase-admin/app';
import {getAuth} from 'firebase-admin/auth';
import {getFirestore} from 'firebase-admin/firestore';
import {parseArgs} from 'node:util';
import {randomBytes} from 'node:crypto';
import {catalog,validateOwner,verifyOwnerLock} from './domain.mjs';

const {values} = parseArgs({options:{project:{type:'string'},email:{type:'string'},name:{type:'string',default:'Carlos Eduardo'}}});
if(!values.project || !values.email) throw new Error('Uso: npm run bootstrap -- --project SEU_PROJECT_ID --email SEU_EMAIL');
// Generated only for a newly created account; never embedded in the Android app.
const password = randomBytes(18).toString('base64url') + 'aA1!';
validateOwner(values.email,password,values.name);
initializeApp({credential:applicationDefault(),projectId:values.project});
const auth=getAuth(); const db=getFirestore(); const lock=db.doc('system/owner');
const prior=await lock.get();
let user; let created=false;
try { user=await auth.getUserByEmail(values.email); }
catch(e) { if(e.code!=='auth/user-not-found') throw e; }
// Do not create or promote a different account if an owner was already provisioned.
if(prior.exists && (!user || prior.get('uid') !== user.uid)) throw new Error('Já existe outro proprietário neste projeto.');
if(!user) { user=await auth.createUser({email:values.email,password,displayName:values.name}); created=true; }
try {
  await db.runTransaction(async tx => {
    const current=await tx.get(lock); verifyOwnerLock(current.exists ? current.get('uid') : null,user.uid);
    tx.set(lock,{uid:user.uid});
    tx.set(db.doc(`access/${user.uid}`),{uid:user.uid,name:values.name.trim(),email:user.email,status:'active'});
  });
} catch(e) { if(created) await auth.deleteUser(user.uid); throw e; }
// Preserve configured prices, migrate v1.0 product docs, and add only the new menu items.
await db.runTransaction(async tx => {
  const menuRef=db.doc('catalog/menu'); const snapshot=await tx.get(menuRef);
  let existing=snapshot.exists ? snapshot.get('products') ?? {} : {};
  if(!snapshot.exists) {
    const refs=Object.keys(catalog).map(id=>db.doc(`products/${id}`));
    const old=await Promise.all(refs.map(ref=>tx.get(ref)));
    existing=Object.fromEntries(old.filter(s=>s.exists).map(s=>[s.id,s.data()]));
  }
  const products=Object.fromEntries(Object.entries(catalog).map(([id,priceCents])=>{
    const old=existing[id];
    return [id, old && Number.isInteger(old.priceCents) && old.priceCents>=100 && old.priceCents<=100000 && typeof old.available==='boolean'
      ? {priceCents:old.priceCents,available:old.available} : {priceCents,available:true}];
  }));
  tx.set(menuRef,{products});
});
console.log(`Proprietário configurado: ${user.email}\nUID: ${user.uid}`);
if(created) console.log(`SENHA INICIAL (guarde em seu gerenciador): ${password}`);
else console.log('Conta existente: a senha foi preservada.');
console.log('Nenhum e-mail foi enviado. Publique as regras antes de distribuir o app.');
