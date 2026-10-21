import test from 'node:test';
import assert from 'node:assert/strict';
import {calculateTotal,validateOwner,verifyOwnerLock,catalog} from '../domain.mjs';

test('total em centavos sem arredondamento de ponto flutuante',()=>assert.equal(calculateTotal({cappuccino:2,espresso:1}),3870));
test('todos os produtos aceitam o limite de dez unidades',()=>assert.equal(calculateTotal(Object.fromEntries(Object.keys(catalog).map(id=>[id,10]))), Object.values(catalog).reduce((a,b)=>a+b,0)*10));
test('rejeita produto desconhecido, zero, negativo, fração, texto e excesso',()=>{
  for(const cart of [{x:1},{espresso:0},{espresso:-1},{espresso:1.5},{espresso:'1'},{espresso:11},{},[],null]) assert.throws(()=>calculateTotal(cart));
});
test('rejeita item indisponível',()=>assert.throws(()=>calculateTotal({espresso:1},{espresso:{priceCents:890,available:false}})));
test('preço do catálogo é a autoridade',()=>assert.equal(calculateTotal({espresso:2},{espresso:{priceCents:990,available:true}}),1980));
test('valida configuração do proprietário',()=>{
  assert.doesNotThrow(()=>validateOwner('carlos@example.com','LongPassword123!','Carlos Eduardo'));
  assert.throws(()=>validateOwner('bad','LongPassword123!','Carlos'));
  assert.throws(()=>validateOwner('carlos@example.com','short','Carlos'));
  assert.throws(()=>validateOwner('carlos@example.com','LongPassword123!','C'));
});
test('bootstrap nunca substitui outro proprietário',()=>{
  assert.doesNotThrow(()=>verifyOwnerLock(null,'carlos'));
  assert.doesNotThrow(()=>verifyOwnerLock('carlos','carlos'));
  assert.throws(()=>verifyOwnerLock('carlos','other'));
});
