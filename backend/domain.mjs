export const catalog = Object.freeze({cappuccino:1490,espresso:890,latte:1390,coldbrew:1690,mocha:1590,cookie:990,croissant:1290,paochapa:990,paoqueijo:790,brownie:1190});
export function validateOwner(email, password, name) {
  if(typeof email !== 'string' || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) throw new Error('Informe um e-mail válido.');
  if(typeof password !== 'string' || password.length < 12 || password.length > 128) throw new Error('Use uma senha de 12 a 128 caracteres.');
  if(typeof name !== 'string' || name.trim().length < 2 || name.trim().length > 80) throw new Error('Nome: 2 a 80 caracteres.');
}
export function verifyOwnerLock(savedUid, requestedUid) {
  if (savedUid && savedUid !== requestedUid) throw new Error('Este projeto já tem outro proprietário. Nenhuma substituição é permitida.');
}
export function calculateTotal(quantities, products = Object.fromEntries(Object.entries(catalog).map(([id, priceCents]) => [id, {priceCents,available:true}]))) {
  if(!quantities || typeof quantities !== 'object' || Array.isArray(quantities) || Object.keys(quantities).length === 0) throw new Error('Carrinho vazio.');
  let total=0;
  for(const [id,q] of Object.entries(quantities)) {
    if(!Object.hasOwn(catalog,id) || !Number.isInteger(q) || q < 1 || q > 10 || !products[id]?.available) throw new Error('Produto ou quantidade inválida.');
    total += q * products[id].priceCents;
  }
  return total;
}
