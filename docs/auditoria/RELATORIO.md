# Revisão técnica de privacidade, direitos e acessibilidade — CoffeeDev

Data: 21/10/2026 · Versão revisada: 1.2.0 · Responsável pelo projeto: Carlos Eduardo.

## Resultado e escopo

Revisão técnica assistida do código-fonte e dos materiais da demonstração Android local, com correções implementadas e evidências reproduzíveis. Não é parecer de advogado, teste de invasão, registro de marca, certificação LGPD, laudo WCAG completo ou declaração de ausência de erros. Carlos Eduardo é o autor e responsável pelo projeto; este relatório não afirma que ele executou pessoalmente todas as verificações, nem que houve auditor independente.

O pacote é preparado para apresentação como projeto educacional de portfólio, com limitações explícitas. Não autoriza uso operacional com dados reais. A variante cloud exige a revisão e os controles abaixo antes de operação; nenhuma instância real foi examinada ou publicada nesta revisão.

## Matriz de achados

| Tema | Situação encontrada | Medida aplicada | Estado residual |
|---|---|---|---|
| Política de privacidade | Ausente na interface | Texto específico ao fluxo local, acessível sem login e traduzido para PT/EN/ES | Revisão jurídica antes de uso real |
| Termos | Ausentes | Demonstração fictícia, titularidade, limites sem renúncia de direitos obrigatórios; leitura antes de solicitar cadastro | Validade contratual depende do contexto efetivo |
| Cookies | Sem inventário | Aviso explica ausência de cookies/rastreadores no código local; sem banner fictício | Reavaliar se adicionar web/SDKs |
| Formulário LGPD | Não implementado | Rascunho de direitos, cópia explícita e contato privado; nenhuma transmissão automática | LinkedIn pode exigir conta e limitar contato |
| Direitos locais | Sem exportação ou exclusão | JSON restrito ao próprio usuário; exclusão de visitante e pedidos/preferências; limpeza total da instalação pelo dono demo | Arquivos exportados não são apagados pelo app; exclusão é lógica |
| Senhas locais | SHA-256 simples com salt | PBKDF2-HMAC-SHA256, 210 mil iterações, salt aleatório de 32 bytes; migração após login válido | Conta administrativa continua pública por ser demonstração |
| Migração de dados | Cópia anterior permanecia em preferências | Limpeza da cópia antiga após transação SQLite; secure_delete habilitado | Não promete apagamento físico em flash |
| Backup e transferência Android | Só allowBackup=false | Regras explícitas de exclusão por domínio para cloud-backup e device-transfer | Revalidar em aparelhos/OEMs; não controla cópias externas |
| Acesso à rede local | Permissão de internet no manifesto comum | Internet restrita ao manifesto cloud | Links externos abrem no navegador/app escolhido pelo usuário |
| Formulário de acessibilidade | Ausente | Tipo específico de relato, sem pedir diagnóstico, CPF ou dado de saúde | Envio manual e canal externo |
| Contraste | Secundário claro sobre branco: 2,897:1; contorno sobre container: 2,992:1 | Novas cores secundárias/contornos, papéis explícitos nos dois temas e testes de contraste | Não mede todos os pixels, elevações/overlays e estados de componentes |
| Toque/semântica | Favorito com 40 dp, descrições genéricas | Favorito 48 dp, rótulos contextualizados por produto, busca rotulada, títulos e botões semânticos | TalkBack e Switch Access manuais pendentes |
| Fonte ampliada | Grade sempre com duas colunas e alturas fixas em ações | Uma coluna com escala >=1,3; nomes sem truncamento; altura mínima nos botões principais | Validar em aparelhos com fonte 200%, rotação e teclado |
| Imagens | Origem descrita, mas sem inventário jurídico | Dez WebPs inventariados por SHA-256, inspeção visual, crédito de geração e limites de direitos | Sem busca reversa exaustiva ou liberação mundial de similaridade |
| Copyright | Sem separação clara entre autoria própria e terceiros | COPYRIGHT.md e THIRD_PARTY_NOTICES.md, OFL e Apache no APK, inventário de dependências | Não se declara exclusividade sobre saídas de IA |
| Identificação | Nome do autor já constava, sem contato na área pública | Carlos Eduardo, pessoa física; LinkedIn escolhido pelo autor; sem CNPJ/endereço inventado | Não é prova de identidade nem nomeação formal de encarregado |
| Dependências administrativas | Auditoria inicial apontou 28 alertas (13 moderados, 14 altos e 1 crítico) | Atualização de pacotes e lockfile, nova varredura e testes | Restam 7 alertas (4 moderados, 3 altos) só nas ferramentas de desenvolvimento/teste; nenhum na árvore Node de produção; ver DEPENDENCIAS.md |
| Banco cloud | Regras impedem exclusão direta e cliente mostra só até 100 pedidos | Mantido acesso restrito; direitos cloud sinalizados para atendimento administrativo, sem simular exclusão | Exige rotina administrativa paginada de acesso/correção/exclusão antes de dados reais |
| GitHub | Pacote ainda não publicado nesta etapa | README de portfólio, avisos, segurança, ignore e evidências | Publicação é próxima etapa; nenhum selo “100% conforme” |

## LGPD e funcionamento real

Não se presume exclusão da LGPD porque o responsável é pessoa física ou porque o trabalho é de portfólio. A eventual hipótese de uso exclusivamente particular e não econômico deve ser examinada no contexto; divulgação e coleta por terceiros podem mudar o enquadramento. O projeto evita dados reais na demonstração e documenta o tratamento efetivo.

A hipótese preliminar para funções estritamente solicitadas é a execução do serviço a pedido do usuário (art. 7º, V), sem usar um aceite genérico como autorização de marketing. A leitura dos termos não gera prova de consentimento para finalidades opcionais. Uso comercial, dados de menores, dados sensíveis e finalidades adicionais exigem análise própria, que não foi realizada como aprovação nesta entrega.

O app não coleta CPF, endereço, telefone, localização, câmera, contatos, pagamentos ou dados de saúde por campos próprios. Campos livres podem receber dados indevidos; as políticas e instruções orientam não inseri-los. O risco não é eliminado por essa orientação.

## Condições antes de usuários reais ou cloud

1. Definir e testar canal privado acessível que não dependa de conta em rede social; o LinkedIn foi escolhido para o portfólio, mas não é certificado como canal suficiente para todos os titulares.
2. Validar bases legais, identificação/contato e eventual encarregado ou dispensa aplicável com assessoria jurídica. Não há nomeação fictícia ou declaração automática de pequeno porte.
3. Definir retenção por categoria, descarte operacional e minimização. No demo não existe rotina de expiração; a retenção até exclusão está declarada.
4. Implementar rotina administrativa autenticada para exportar TODOS os pedidos, corrigir cadastro, eliminar Auth + access + orders, controlar exceções justificadas e registrar atendimento. Não usar a consulta de 100 pedidos do cliente como exportação completa cloud.
5. Confirmar região do Firestore, tratamento internacional do Auth e metadados, termos do Google e mecanismo jurídico de transferência internacional. O selo/certificação do fornecedor não certifica o CoffeeDev.
6. Manter chaves privadas de assinatura fora do repositório e usar assinatura de lançamento privada; restringir IAM, chaves de serviço, contas e ambientes. Testar restauração e evitar log de dados pessoais.
7. Repetir análise de dependências, licença e regras, configurar controles de abuso apropriados e verificar as configurações reais. Esta revisão de arquivos não confirma IAM, região, cotas ou regras de uma conta online.
8. Executar testes manuais de navegação/semântica/foco, fonte grande, teclado, TalkBack e Switch Access, documentando aparelho, versão e resultados reais.
9. Submeter nome/marca e imagens a pesquisa especializada se houver exploração comercial ou exigência de exclusividade. Não foi feita pesquisa INPI nem declaração de disponibilidade da marca.

## Atendimento e incidentes

Use GESTAO-LGPD.md para registrar pedidos, prazos e descarte sem expor dados em GitHub. Pedido preparado no formulário é rascunho, não protocolo recebido. Não há envio de e-mail ou serviço de tickets implícito.

Se ocorrer incidente real: conter acesso, preservar evidências minimizadas, identificar dados/titulares/causas e avaliar risco ou dano relevante. Consultar a regulamentação ANPD vigente e comunicar quando aplicável, observando prazos e eventuais regras específicas de enquadramento. Não esperar a conclusão forense para avaliar obrigação de comunicação. Nenhum incidente real foi detectado ou declarado nesta revisão.

## Ajuste de publicação no GitHub

A chave privada de assinatura foi excluída da preparação pública. O projeto usa a chave debug local padrão do Android Gradle Plugin para novas compilações. O APK distribuído mantém sua assinatura validada; APKs recompilados podem ter outra assinatura e exigir uma instalação separada, conforme INSTALACAO.md. O ajuste de publicação não declara nova execução de todos os testes de runtime.

## Evidências

- checagens.json: verificações de fontes, traduções e pares de cores, com resultado por item.
- imagens.json e IMAGENS.md: arquivos, hashes, origem e limites da revisão visual.
- ACESSIBILIDADE.md: métricas e verificação manual pendente.
- INVENTARIO-DADOS.md: categorias, finalidade, armazenamento, acesso e descarte.
- DEPENDENCIAS.md e inventários: ferramentas, versões e alertas.
- ../VALIDACAO.md: comandos e resultados de compilação/testes desta versão.
- FONTES.md: fontes oficiais consultadas.

A evidência vale para os arquivos e versões identificados. Alterar funcionalidades, SDKs, dados, implantação ou documentos exige nova revisão. Não existem “risco zero”, “100% LGPD” ou “sem erros” atestados por este relatório.
