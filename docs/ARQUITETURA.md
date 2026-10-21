# Arquitetura e identidade

## Aplicativo

`MainActivity` inicia Compose. `CoffeeViewModel` concentra preferências, carrinho e operações.
A interface observa StateFlow do repositório. O sabor `demo` implementa o repositório em SQLite local;
o sabor `cloud` usa os SDKs oficiais de Authentication e Firestore.
O código do demonstrativo não participa do build conectado.

Preferências locais: idioma, tema, favoritos e carrinho por UID. No banco SQLite local: contas, sessão, catálogo e pedidos. Na versão cloud: Firebase Auth e Firestore.
Falhas de autenticação não expõem dados técnicos ao usuário. Os estados pendente/suspenso não renderizam
cardápio, pedidos ou administração. As regras do Firestore controlam o acesso independentemente da interface.

## Coleções

| Caminho | Dados | Escrita |
|---|---|---|
| `system/owner` | UID do único proprietário | Apenas Admin SDK |
| `access/{uid}` | UID, nome, e-mail, status | Novo usuário cria pendente; proprietário altera somente status de terceiros |
| `catalog/menu` | mapa dos dez produtos, preços em centavos e disponibilidade | Proprietário altera o catálogo validado |
| `orders/{uuid}` | UID, quantidades, total, status, data, observação | Ativo cria o próprio pedido; proprietário avança somente o status |

Os nomes, descrições, categorias e imagens dos dez produtos ficam no aplicativo, traduzidos.
O catálogo desta versão tem SKUs fixos para permitir validação de preço no plano gratuito sem servidor adicional.
Adicionar um novo tipo de produto requer ampliar catálogo, traduções e regras juntos.
Não há meios de pagamento, endereço, estoque quantitativo ou entrega nesta versão: trata-se de retirada no balcão.

## Pedidos

Valores são inteiros em centavos. Quantidades ficam entre 1 e 10 por SKU.
As regras recalculam o total a partir dos documento único de catálogo e rejeitam preços forjados,
itens indisponíveis, identidades falsas e criação fora do estado inicial `received`.
O ID da solicitação é preservado entre tentativas e reinicializações do aplicativo, enquanto o carrinho não for alterado, evitando repetir a criação após falha de rede.
O histórico remoto mostra os 100 pedidos mais recentes, com índice por usuário e data.
Após suspensão, os listeners e os dados em memória são removidos.

## Identidade visual

| Função | Claro | Escuro |
|---|---|---|
| Fundo | `#F7F3EC` | `#14110E` |
| Superfície | `#FFFFFF` | `#241D17` |
| Texto | `#2B1D14` | `#F5EEE5` |
| Primária | `#6B3F21` | `#E0AF76` |
| Secundária acessível | `#845324` | `#E0AF76` |
| Contorno | `#776354` | `#A38B76` |

Símbolo vetorial original: xícara com vapor em forma de código.
A interface inclui Sora para títulos e Inter para textos, como na prancha aprovada.
Os arquivos são distribuídos sob a SIL Open Font License, com cópias em docs/licenses.
Fontes originais: https://github.com/google/fonts/tree/main/ofl/sora e https://github.com/google/fonts/tree/main/ofl/inter.
As fotografias realistas foram criadas para os dez produtos e são empacotadas em WebP, funcionando offline em todas as telas.

Os temas são fixos da marca, sem recoloração dinâmica do Android.
A moeda permanece BRL nos três idiomas. Apenas o formato e os textos acompanham o idioma.
Os textos de interface/catálogo são centralizados em Strings.kt; conteúdos escritos pelo usuário
(nome e observações) permanecem como digitados.


## Banco local e migração

O sabor local usa SQLite (`coffeedev-local.db`), com snapshots gravados em transações.
Na primeira execução, `LocalDatabase` importa as contas, pedidos, catálogo e sessão do armazenamento local anterior, quando esses dados estiverem presentes.
Preferências de aparência, idioma, favoritos e carrinho continuam nas preferências privadas do Android.

## Ícone do aplicativo

O launcher usa background opaco, foreground vetorial e variante roundIcon. Em aparelhos com ícones temáticos, há uma camada monocromática.
A máscara final é escolhida pelo launcher: em launchers circulares, o fundo cobre a bolinha inteira; outros launchers podem usar outra forma.

## Catálogo cloud 1.1

O catálogo fica em `catalog/menu`, com dez entradas. Isso permite validar o total de um pedido de dez produtos com uma única leitura de catálogo nas regras.
O script de bootstrap preserva preços antigos e adiciona apenas itens ausentes.
As regras continuam sendo um protótipo do projeto, verificado nos emuladores; não representam uma auditoria exaustiva de produção.


## Privacidade e acessibilidade 1.2

A área LegalCenter abre antes do login e em contas pendentes/bloqueadas pelo botão Info do topo. Políticas locais PT/EN/ES são arquivos incorporados; ler não abre um site. O formulário fica em memória e só copia texto por ação explícita, sem transmitir conteúdo ao LinkedIn. O contato externo é uma URL fixa escolhida pelo autor.

Exportação local consulta apenas o UID autenticado e seus pedidos, sem hashes/salts. A exclusão de visitante remove conta, pedidos, sessão e preferências por UID; tema/idioma gerais permanecem. O dono fixo pode usar a limpeza total Android, que encerra o processo. Arquivos exportados não são apagados. O servidor cloud não é apagado por essas ações.

PBKDF2-HMAC-SHA256 com 210 mil iterações substitui o SHA-256 simples; credenciais antigas são atualizadas após login válido. A migração elimina a cópia antiga em preferências. SQLite usa secure_delete para exclusão lógica, sem alegar apagamento físico de flash. A variante local não tem Internet; o manifesto cloud declara essa permissão.

Cores e pares de contraste têm testes; alvos/rótulos e grade adaptada a fontes grandes foram melhorados. Testes manuais de acessibilidade permanecem pendentes, conforme auditoria/ACESSIBILIDADE.md. O app não trata imagens como fotografias de produtos reais.
