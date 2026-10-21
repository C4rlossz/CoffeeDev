import {test} from 'node:test';
import assert from 'node:assert/strict';
import {promisify} from 'node:util';
import {execFile} from 'node:child_process';
import {initializeApp} from 'firebase-admin/app';
import {getFirestore} from 'firebase-admin/firestore';
import {getAuth} from 'firebase-admin/auth';
const execute=promisify(execFile);

test('owner provisioning is idempotent, preserves prices, and refuses a second owner',async()=>{
  assert.ok(process.env.FIRESTORE_EMULATOR_HOST && process.env.FIREBASE_AUTH_EMULATOR_HOST,'Requires Auth and Firestore emulators');
  const project='demo-coffeedev-bootstrap';
  initializeApp({projectId:project});
  const db=getFirestore(); const auth=getAuth();
  const run=email=>execute(process.execPath,['bootstrap-owner.mjs','--project',project,'--email',email,'--name','Carlos Eduardo'],{cwd:new URL('..',import.meta.url),env:process.env});
  // An older installed backend may have one document per product.
  await db.doc('products/espresso').set({priceCents:910,available:true});
  const first=await run('carlos@example.com');
  assert.match(first.stdout,/SENHA INICIAL/);
  const user=await auth.getUserByEmail('carlos@example.com');
  assert.equal((await db.doc('system/owner').get()).get('uid'),user.uid);
  assert.equal((await db.doc(`access/${user.uid}`).get()).get('status'),'active');
  assert.equal(Object.keys((await db.doc('catalog/menu').get()).get('products')).length,10);
  assert.equal((await db.doc('catalog/menu').get()).get('products.espresso.priceCents'),910);
  await db.doc('catalog/menu').update({'products.espresso.priceCents':990});
  const repeat=await run('carlos@example.com');
  assert.doesNotMatch(repeat.stdout,/SENHA INICIAL/);
  assert.equal((await db.doc('catalog/menu').get()).get('products.espresso.priceCents'),990);
  await assert.rejects(run('other@example.com'));
  assert.equal((await auth.listUsers()).users.length,1);
  assert.equal((await db.doc('system/owner').get()).get('uid'),user.uid);
  await db.terminate();
});
