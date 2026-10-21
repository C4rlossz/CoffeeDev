# Dependências e alertas

Data: 21/10/2026. Npm consultado no registro oficial; resultados dependem da base de alertas disponível nesta data. Resultado sem alertas não significa ausência de vulnerabilidade.

## Backend e ferramentas opcionais

- Atualizados firebase-admin 13.4.0 → 14.5.0, firebase 11.8.1 → 13.1.0, @firebase/rules-unit-testing 4.0.1 → 6.0.0 e firebase-tools 14.4.0 → 15.34.0, com lockfile preservado.
- Override explícito de @grpc/grpc-js 1.14.6 substitui versões transitivas vulneráveis; a suíte de regras e domínio foi repetida após essa mudança. Reavaliar/remover o override quando as dependências principais incorporarem a versão corrigida.
- Ambiente recomendado: Node.js 22 LTS; JDK 21 para os emuladores Firebase atuais. Build Android conserva alvo Java 17 e pode ser executado com JDK compatível.
- Auditoria inicial: 28 alertas (13 moderados, 14 altos, 1 crítico).
- Auditoria final: **7 alertas, 4 moderados e 3 altos; nenhum crítico**. São da árvore de desenvolvimento/testes do firebase-tools; **npm audit --omit=dev não apontou alertas** na árvore de produção do backend.
- Não foi aplicado npm audit fix --force: as sugestões incluíam downgrade com mudança de versão principal e não garantem correção adequada das funções usadas. Nenhuma implementação local improvisada de biblioteca foi adotada.
- Essas dependências Node não são empacotadas no APK Android local. Isso não elimina risco para a máquina de desenvolvimento: usar ambiente isolado e dados fictícios, manter controles de acesso e não expor ferramentas como serviço público.

| Pacote reportado | Severidade | Origem/aviso |
|---|---|---|
| @google-cloud/pubsub | moderate | @opentelemetry/core |
| @opentelemetry/core | moderate | [OpenTelemetry Core: Unbounded memory allocation in W3C Baggage propagation](https://github.com/advisories/GHSA-8988-4f7v-96qf) |
| braces | high | [braces vulnerable to stack-exhaustion denial of service through deeply nested patterns](https://github.com/advisories/GHSA-vfj7-8cjw-p6xm) |
| chokidar | high | braces |
| firebase-tools | high | @google-cloud/pubsub; chokidar; gaxios |
| gaxios | moderate | uuid |
| uuid | moderate | [uuid: Missing buffer bounds check in v3/v5/v6 when buf is provided](https://github.com/advisories/GHSA-w5hq-g745-h8pq) |

Os relatórios JSON completos e o SBOM CycloneDX acompanham este diretório. Alguns pacotes são marcados por propagação do mesmo alerta transitivo; o total não é contagem de explorações comprovadas. Nenhuma análise de explorabilidade completa foi realizada. A árvore Node de teste com alertas é risco residual explícito, não aprovada para dados reais nesta revisão.

## Android

O inventário android-dependencies.json lista os artefatos resolvidos para demoDebug, versões e hashes. Os avisos Apache 2.0 e OFL estão incorporados no APK, além de THIRD_PARTY_NOTICES.md. Não foi realizada análise SCA/CVE exaustiva de todos os binários Android, nem revisão de código de todas as dependências.

O aplicativo local usa AndroidX/Compose/Kotlin; não inclui SDK Firebase Auth/Firestore. A variante cloud deve ter seu próprio inventário e nova análise antes de publicação operacional. A certificação de um provedor não certifica este projeto.
