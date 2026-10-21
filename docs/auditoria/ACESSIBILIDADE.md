# Acessibilidade: escopo e evidências

WCAG 2.2 é usada como referência técnica, junto da documentação Android/Compose; não se afirma certificação WCAG nem auditoria independente. Uma análise de contraste de paleta não verifica todo o aplicativo.

## Alterações aplicadas

- Contraste secundário claro corrigido de 2,897:1 contra branco para um marrom escuro com texto branco adequado. Contorno claro corrigido onde tinha 2,992:1 contra o container.
- Cores explícitas para pares primary, secondary, tertiary, containers, surface, background, error e inverse, nos dois temas. Os limiares dos pares de texto são 4,5:1; contornos essenciais, 3:1.
- Os números completos, sem arredondamento na aprovação, estão em checagens.json. ContrastTest valida também texto primário/secundário sobre superfícies usadas pelo app.
- Favoritos com área de 48 dp e fundo opaco estável. Os demais botões próprios usam os padrões Material ou mínimos de altura de 48 dp.
- Descrições de adicionar/favoritar incluem o produto, em PT/EN/ES. Imagens de produtos têm descrição traduzida; ícones decorativos evitam anúncio duplicado.
- Busca com rótulo persistente; títulos de páginas com semântica de heading e links do perfil com papel de botão.
- Grade muda para uma coluna com escala de fonte >=1,3. Nome do produto sem truncamento de uma linha. Botões principais e legais usam altura mínima em vez de altura rígida.
- Formulários possuem rótulos textuais, valores revisáveis e confirmação antes de exclusão. O rascunho de solicitação não é enviado sem ação manual.

## Verificação manual necessária

Nenhum item abaixo é marcado como realizado sem evidência em aparelho:

| Cenário | Critério de aceitação | Estado |
|---|---|---|
| TalkBack em login, cadastro, cardápio e privacidade | Ordem útil, nomes/estados corretos, diálogo com retorno de foco | Pendente em aparelho |
| Fonte Android 200% e tela estreita | Conteúdo acessível por rolagem sem sobreposição ou corte funcional | Pendente em aparelho |
| Teclado e Switch Access | Todas as ações alcançáveis e acionáveis, foco visível | Pendente em aparelho |
| Claro/escuro e estados reais | Contraste de todos os componentes, elevações, snackbars, erros e foco | Pendente em aparelho |
| PT/EN/ES com leitor de tela | Pronúncia e anúncio de estados adequados | Pendente; depende também da voz/configuração do aparelho |
| Exportar/excluir com tecnologia assistiva | Seletor compreensível, confirmação clara, resultado anunciado | Pendente em aparelho |
| Avaliação por pessoas com deficiência | Barreiras identificadas e corrigidas | Não realizada |

O canal externo LinkedIn pode exigir conta e tem acessibilidade própria. Antes de operar com dados reais, oferecer alternativa privada acessível que não dependa de conta nessa plataforma.

O formulário de acessibilidade usa o mesmo fluxo privado de solicitações: descrever barreira e resultado esperado, sem pedir diagnóstico/saúde. A interface não apresenta selo oficial ou conformidade integral.
