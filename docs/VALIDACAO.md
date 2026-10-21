# Validação da entrega 1.2.0

Data: 21/10/2026. Esta é uma revisão técnica assistida de demonstração local; não é certificação LGPD/WCAG, auditoria independente ou parecer jurídico.

## Resultados executados

| Verificação | Resultado |
|---|---|
| Build Android demo limpo | Sucesso, APK 1.2.0-demo / versionCode 3 |
| Testes Android demo | 19 aprovados, zero falhas/erros |
| Testes compartilhados cloud | 7 aprovados, zero falhas/erros |
| Android Lint demo | Zero erros; 17 avisos |
| Android Lint cloud | Zero erros; 11 avisos |
| Testes Node de domínio | 7 aprovados |
| Auth/Firestore emuladores oficiais | 16 aprovados após atualização das dependências e override gRPC |
| Assinatura do APK | Válida; mesma chave da versão 1.1 |
| Permissões APK local | Sem INTERNET; somente permissão interna AndroidX de receptor não exportado |
| Recursos do APK | Dez imagens WebP, 15 documentos legais PT/EN/ES e avisos de fontes/licenças incluídos |
| Integridade do APK e ZIP | Verificada |
| Inventário Android | 63 artefatos de runtime demo com versões e hashes |
| Varredura textual de segredos | Nenhum achado nos padrões verificados; conta demo pública documentada e chave privada excluída do repositório |
| Npm produção opcional | Nenhum alerta reportado com --omit=dev |
| Npm desenvolvimento/testes | 7 alertas residuais (3 altos e 4 moderados); nenhum crítico |

Os 19 testes incluem catálogo e tradução, pares de contraste, carregamento de ícones adaptáveis, persistência/migração SQLite, pedidos e aprovação/suspensão de contas. Os cinco testes de privacidade cobrem exportação sem dados de outras contas e sem credenciais, exclusão de conta pendente/preferências, exclusão de pedidos com persistência após reabrir, migração de hash legado e limpeza da cópia antiga mesmo quando o banco já foi migrado pela versão 1.1.

A correção do comando PRAGMA foi validada com execução de SQLite no ambiente Android de testes. O build final foi limpo para evitar estado incremental antigo. As tentativas iniciais encontraram limitações de preparação do ambiente (JRE sem compilador e download de dependência de teste sem proxy); o ambiente de build foi corrigido e os resultados acima são os da execução final concluída. As correções funcionais do código foram mantidas nos arquivos entregues.

Os avisos Lint tratam de estilo/API, versões de dependências e recursos de ícone; o ícone monocromático existe na configuração v33. commit() foi mantido nos pontos de remoção de dados para aguardar a gravação, apesar da recomendação geral por apply(). Não se declara ausência de avisos ou de vulnerabilidades.

## Cloud e limites

A variante cloud foi compilada/testada com configuração fictícia local, removida ao terminar; nenhum APK dessa configuração é entregue. Os emuladores usam apenas o projeto fictício demo-coffeedev. Nenhuma conta, regra ou banco real foi provisionado, auditado ou publicado.

Não houve teste manual completo em aparelho, TalkBack ou Switch Access. Robolectric e testes estáticos não substituem esses testes, avaliação jurídica, análise exaustiva de dependências, pesquisa de marca ou busca reversa de imagens. Veja auditoria/RELATORIO.md e auditoria/ACESSIBILIDADE.md.

## Reproduzir

- scripts/audit-project.py verifica documentação incorporada, traduções, manifesto, cores e imagens (Python 3).
- Raiz Android: ./gradlew clean testDemoDebugUnitTest lintDemoDebug assembleDemoDebug.
- Windows: use .\gradlew.bat no lugar de ./gradlew.
- Com configuração própria Firebase: ./gradlew testCloudDebugUnitTest lintCloudDebug.
- Backend: npm ci, npm test e npm run test:rules, usando Node.js 22 LTS e JDK 21.
- Backend: npm audit --json e npm audit --omit=dev --json; a base de alertas muda com o tempo.

O workflow GitHub Actions inclui verificações de fonte, build/testes demo e suíte de regras. Evidências estruturadas estão em auditoria/testes-android.json, checagens.json, imagens.json, segredos.json e nos inventários de dependências. Os testes podem precisar baixar ferramentas oficiais na primeira execução.


## Uso Android informado pelo autor

Na preparação da publicação no GitHub, Carlos Eduardo informou o uso do aplicativo em Android 15 (API 35) e Android 16 (API 36). Esse registro é um relato do autor, separado dos testes automatizados Android/Robolectric executados em API 34 nesta revisão. Não foram fornecidos modelo do aparelho, logs ou protocolo detalhado desses testes manuais. O relato não representa teste instrumentado completo nem validação de TalkBack/Switch Access.

O APK permanece compilado com compileSdk/targetSdk 35; executar em Android 16 não altera essa configuração.


## Assinatura na publicação do código

A assinatura compatível com a versão 1.1 refere-se ao APK já validado e disponibilizado em downloads. O repositório público não contém a chave privada. Para novos builds, o Gradle usa a chave debug local padrão; esses APKs podem ter outra assinatura. A preparação para publicação alterou a configuração de assinatura, a documentação e a instalação dos pacotes do SDK no GitHub Actions; os resultados acima pertencem ao APK 1.2.0 distribuído.
