# Ativar sua instância Firebase

**Pré-condição:** a revisão 1.2 autoriza apenas demonstração local com dados fictícios. Antes de usuários reais, cumpra as pendências cloud em auditoria/RELATORIO.md (atendimento de direitos no servidor, retenção, contratos, transferências, canal e segurança). Este guia técnico não certifica conformidade jurídica.

O código da integração está incluído. Esta entrega não contém um projeto Firebase provisionado,
sua conta real criada, credenciais de serviço ou regras já publicadas em uma conta sua.
A versão demonstrativa funciona separadamente e não simula uma conexão real com a nuvem.

## 1. Projeto e plano

Crie ou selecione seu projeto no console Firebase. Mantenha o plano **Spark**, sem habilitar faturamento.
Use Cloud Firestore Standard no banco padrão `(default)`, com localização escolhida uma vez na criação.
Esta aplicação não usa Functions, Storage, SMS, App Hosting ou outro serviço que requeira faturamento.
Há franquias de uso: gratuito não significa ilimitado. Consulte a página de cotas vinculada abaixo.

## 2. Authentication

Em Authentication → Sign-in method, ative **Email/Password**.
Não é necessário configurar envio/verificação por e-mail. O aplicativo não chama essas operações.
A senha da conta real não fica no código.

## 3. Registrar Android

Registre um aplicativo Android com o pacote `dev.coffeedev`.
Baixe `google-services.json` e coloque-o em `app/google-services.json`.
Esse arquivo identifica o projeto Android; ele não substitui as regras de acesso.
Jamais coloque credenciais do Admin SDK dentro do aplicativo ou do repositório.
O build `cloud` falha com uma mensagem clara se a configuração Android estiver ausente.

## 4. Instalar ferramentas administrativas

Requer Node.js 22 LTS e JDK 21 para a suíte de emuladores atualizada e uma identidade Google com permissão administrativa no projeto.
Na pasta `backend`:

```sh
npm ci
npx firebase login
```

Se não houver `package-lock.json`, use `npm install` no lugar de `npm ci`.
O login da CLI Firebase e as credenciais do Admin SDK são configurações diferentes.
Para o Admin SDK, prefira credenciais locais do Google Cloud:

```sh
gcloud auth application-default login
```

Alternativamente, gere uma chave de conta de serviço no console, mantenha-a fora do projeto e aponte
`GOOGLE_APPLICATION_CREDENTIALS` para o arquivo local. Nunca envie a chave por chat.

## 5. Publicar regras e índices

Partindo da raiz do projeto:

```sh
npx --prefix backend firebase deploy --project SEU_PROJECT_ID --only firestore:rules,firestore:indexes
```

Selecione o projeto explicitamente. Não ative regras em modo de teste nem liberação pública.
O índice dos pedidos por usuário pode levar alguns minutos para ficar disponível.

## 6. Criar sua única conta proprietária

Na pasta `backend`:

```sh
npm run bootstrap -- --project SEU_PROJECT_ID --email SEU_EMAIL --name "Carlos Eduardo"
```

O script gera uma senha aleatória e a exibe somente no terminal na criação da conta.
Guarde-a no seu gerenciador de senhas. Nenhum e-mail é enviado.
Se a conta já existe, a senha atual é preservada.
Ele cria o proprietário em `system/owner`, libera sua conta e preenche os dez produtos em `catalog/menu`. Se houver documentos `products/{id}` da versão 1.0, os preços e a disponibilidade são preservados na migração.
Executar de novo não troca outro proprietário, não redefine senha e não sobrescreve preços já configurados.

Se perdeu a senha, redefina-a administrativamente no console/SDK. O app não tem fluxo de recuperação por e-mail.

## 7. Compilar e usar

Gere e instale `cloudDebug` conforme INSTALACAO.md. Entre com o e-mail e a senha reais.
Novos cadastros entram como `pending`; apenas sua conta proprietária libera ou suspende o acesso.
Aprovação dá acesso ao aplicativo e aos próprios pedidos; não concede administração.

## Cotas e fontes oficiais

- https://firebase.google.com/docs/firestore/quotas
- https://firebase.google.com/docs/auth/android/password-auth
- https://firebase.google.com/docs/android/setup
- https://firebase.google.com/docs/firestore/security/rules-conditions
- https://firebase.google.com/docs/emulator-suite

O envio de pedidos reais requer conexão com a internet. O app conectado não mantém cache persistente
de dados do negócio e não concede acesso aprovado usando um estado local demonstrativo.
