# Instalar e gerar o aplicativo

## Conta local de demonstração

Use apenas dados fictícios. O acesso administrativo é público nesta instalação:

- E-mail: `carlos@coffeedev.demo`
- Senha: `CoffeeDev2026!`

As credenciais não aparecem na tela de login. O proprietário demonstrativo administra todas as contas e pedidos desta instalação; não use dados reais ou uma senha reutilizada.

## Instalar APK no celular

1. Transfira `CoffeeDev-atualizado.apk` para seu Android 8.0 ou superior, se ele acompanhar a entrega.
2. Abra o arquivo e permita a instalação por esse aplicativo de arquivos quando o Android solicitar.
3. Abra **CoffeeDev** e preencha o e-mail e a senha informados na seção anterior deste guia.
4. Entre, experimente os temas e idiomas no topo ou no Perfil.

A versão 1.2 preserva a chave da 1.1 e pode atualizá-la sem remover dados, se instalada a partir desse pacote. A assinatura de debug serve para testes. A publicação em loja exige assinatura de lançamento própria.

## Atualizar uma instalação anterior

O APK 1.0 foi assinado com uma chave de teste que não está disponível nesta atualização.
Se o Android exibir conflito de assinatura, a versão anterior precisa ser desinstalada antes da nova.
**Desinstalar remove os dados locais da versão anterior.** Não desinstale se você precisar manter esses dados sem antes providenciar uma cópia.
O APK distribuído da versão 1.2 possui a mesma assinatura do APK 1.1 e pode atualizá-lo. A chave privada usada nesses APKs não acompanha o repositório público.

Uma nova compilação debug gera/usa a chave local padrão do Android Gradle Plugin. Sua assinatura pode ser diferente da do APK distribuído ou de compilações feitas em outro computador/CI. Nesse caso, use um aparelho de teste separado ou desinstale a instalação anterior, ciente de que isso apaga seus dados locais. Não se promete atualização direta entre APKs com chaves diferentes.

Assinatura de lançamento deve usar chave privada mantida fora do repositório e do APK; nunca publique arquivos de keystore ou senhas de lançamento.

## Abrir o projeto

Instale Android Studio com suporte a AGP 8.9.2, JDK 17 e SDK Android 35.
Abra a pasta `CoffeeDev`, aguarde o Gradle sincronizar e escolha `demoDebug` em Build Variants.
Conecte o celular com depuração USB ou inicie um emulador e clique em Run.

## Gerar pela linha de comando

Na raiz do projeto, com o SDK configurado em `ANDROID_HOME` ou `local.properties`:

```powershell
# Windows
.\gradlew.bat testDemoDebugUnitTest assembleDemoDebug
```

```sh
# Linux ou macOS
./gradlew testDemoDebugUnitTest assembleDemoDebug
```

O inicializador baixa o Gradle 8.11.1 oficial e verifica seu SHA-256 fixado.
Requer internet na primeira execução; Linux/macOS também requerem curl e unzip.

Saída: `app/build/outputs/apk/demo/debug/app-demo-debug.apk`.

Após ativar Firebase, use `assembleCloudDebug` e instale o APK em
`app/build/outputs/apk/cloud/debug/app-cloud-debug.apk`.

Há uma configuração GitHub Actions que testa e gera o APK demonstrativo ao colocar o conteúdo
na raiz de um repositório. Em Actions, o download fica no artefato `CoffeeDev-demo-APK`.
A geração na nuvem não publica o aplicativo nem ativa Firebase.
