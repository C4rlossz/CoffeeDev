# Segurança

A versão distribuída é uma demonstração local, com credenciais administrativas públicas e dados fictícios. Não use para dados reais, confidenciais, pessoais de terceiros, pagamentos ou operação de cafeteria.

Para relatar vulnerabilidade, contate Carlos Eduardo em mensagem privada no LinkedIn: https://www.linkedin.com/in/carlos-eduardo-a8bb50268/ . Não publique senhas, dados de usuários, chaves ou detalhes exploráveis em issues públicas. O LinkedIn é externo e pode exigir conta; não é um canal com SLA garantido. Um canal privado independente é pendência antes de operação real.

Somente a versão atual do projeto é revisada; não há compromisso contratual de suporte. A revisão técnica descreve riscos residuais em docs/auditoria/RELATORIO.md. Não há certificado de segurança ou promessa de ausência de erros.

A variante cloud é uma base educacional e não está liberada por esta auditoria para dados reais. Antes de ativação: implementar atendimento de direitos no servidor, validar contratos/regiões/transferências, revisar dependências, IAM e regras, configurar resposta a incidentes e assinatura de lançamento própria. Chaves privadas de assinatura não são incluídas no repositório. Builds debug usam a chave de teste local gerada pelo Android Gradle Plugin; uma assinatura de lançamento deve ser configurada fora do controle de versão.
