# Inventário de dados e operações

Escopo: versão 1.2.0 local, sem internet, destinada a dados fictícios. Carlos Eduardo decide o desenho do projeto; isso não o transforma em empresa. Enquadramento jurídico definitivo e cenário real não foram certificados.

| Categoria | Finalidade | Onde fica / quem acessa | Retenção / remoção |
|---|---|---|---|
| Nome e e-mail de teste, UID, status, papel | Identificar conta, aprovação e acesso | SQLite privado; próprio usuário e administrador público da instalação | Até exclusão da conta, limpeza ou desinstalação |
| Derivação de senha, salt, algoritmo, iterações | Verificação de acesso local | SQLite privado; não exportado pelo app | Excluído com conta; hash legado atualizado após login válido |
| Sessão/UID | Continuar acesso | SQLite privado | Sai no logout, exclusão ou limpeza |
| Pedido/ID, itens, valor, data, status, nota | Simular retirada e histórico | SQLite; visitante vê seus pedidos, dono vê todos | Até exclusão da conta ou limpeza; não há expiração automática |
| Idioma e tema | Preferência visual | SharedPreferences privado | Até limpeza/desinstalação; não é apagado ao excluir só um usuário |
| Favoritos, carrinho, ID de tentativa por UID | Preferências e evitar duplicação | SharedPreferences privado | Removidos na exclusão da conta local ou limpeza |
| Preços/disponibilidade editados | Administrar cardápio simulado | SQLite privado | Até limpeza/desinstalação |
| Rascunho de direitos/barreira e contato opcional | Preparar mensagem ao autor | Memória da tela; sem envio automático | Descartado ao sair da tela; cópia explícita vai para clipboard do aparelho |
| Arquivo JSON exportado | Cópia de acesso do próprio usuário | Destino escolhido no seletor Android | Sob controle de quem salva; apagar separadamente |
| Mensagem ao LinkedIn | Responder solicitação, se efetivamente enviada | Plataforma externa escolhida, conta do autor | Definir descarte após atendimento/exceção legal; não há automação no app |

Dados não solicitados em campos próprios: CPF, endereço, telefone, localização, documentos, saúde, contatos, câmera, dados financeiros ou publicidade. Campo livre ainda permite inserção indevida; orientar e minimizar.

Base preliminar das funções necessárias: art. 7º, V, quando aplicável ao serviço solicitado; não há coleta baseada em um aceite genérico de política. Consentimento para marketing não existe. Não se presumiu exceção do art. 4º, nem realizou teste formal de legítimo interesse.

Na variante cloud opcional, Auth/Firestore e metadados de infraestrutura entram no fluxo. A localização, contratos, transferências, IAM, retenção e direitos precisam de inventário da implantação real. A conta do autor pode acessar cadastros e pedidos para administração; isso deve ser informado antes do cadastro real. A demonstração local não transmite seu banco ao autor.
