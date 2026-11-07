# Bora! Delivery

Aplicacao demonstrativa de marketplace de delivery com frontend React/Vite, API Java/Spring Boot e banco H2 em memoria. O catalogo de demonstracao gera 400 restaurantes e 10.000 itens de cardapio; os registros, contas e pedidos do H2 sao recriados quando a API reinicia.

> Projeto independente, sem afiliacao ao iFood. Cupons, pagamentos e operacao do entregador sao demonstrativos; a posicao do entregador e simulada, nao vem de GPS real.

## Executar localmente

Pre-requisitos: Node.js 18+, npm, Java 17+ e Maven 3.9+.

1. Inicie a API:

   ```powershell
   cd backend
   mvn spring-boot:run
   ```

   A API fica em `http://localhost:8080`. `GET /api/health` confirma que esta ativa.

2. Na raiz do projeto, instale dependencias e inicie o frontend:

   ```powershell
   npm install
   npm run dev
   ```

   Abra a URL impressa pelo Vite (normalmente `http://localhost:5173`). O Vite encaminha `/api` para a API.

3. Execute os testes de integracao:

   ```powershell
   cd backend
   mvn test
   ```

## Contas demonstrativas

| Perfil | E-mail | Senha |
|---|---|---|
| Cliente | `cliente@bora.demo` | `cliente123` |
| Operacao do restaurante | `restaurante@bora.demo` | `restaurante123` |
| Administracao | `admin@bora.demo` | `admin123456` |

Tambem e possivel criar uma conta de cliente pela tela de cadastro. Senhas sao armazenadas com BCrypt; a sessao usa token Bearer opaco, valido por 12 horas e mantido em memoria. Tokens deixam de funcionar quando a API reinicia.

## Funcionalidades implementadas

- Descoberta do catalogo com busca, categorias, ordenacao, destaques e favoritos; restaurantes e cardapios sao paginados.
- 400 estabelecimentos ficticios e 10.000 itens de cardapio demonstrativos gerados no backend. Os pratos e faixas de preco foram diversificados por cozinha; nomes, disponibilidade e precos nao correspondem a comerciantes reais.
- Sacola com quantidades e personalizacao de itens, selecao de endereco no mapa ou pela localizacao do navegador e validacao de cupom.
- Banners e promocoes-surpresa demonstrativas. Codigos: `BEMVINDO10`, `BORAALMOCO` e `SURPRESA15`; o backend valida e calcula os descontos.
- Criacao e historico de pedidos por conta, painel demonstrativo de operacao e transicoes de status validadas no backend.
- Cadastro e edicao autenticada de perfil (nome, e-mail, celular e endereco sugerido); o checkout pode preencher esses dados automaticamente.
- Celular e endereco de perfil sao criptografados com AES-256-GCM no banco. Em desenvolvimento, a chave e efemera e compativel apenas com o H2 em memoria.
- Cadastro demonstrativo de cartoes armazena somente bandeira e quatro ultimos digitos, criptografados; nao recebe PAN/CVV, nao gera token de pagamento e nao pode ser usado para cobrar.
- Lancamentos de pagamento simulados para Pix, cartao, dinheiro, BB Pay, NuPay e Alelo; o historico nao representa cobrancas ou transacoes reais.
- Comprovante demonstrativo por pedido com referencia `BORA-DEMO-*`, sem validade fiscal e sem calculo/imposicao de tributos.
- Controle de estoque paginado no painel: ajustar quantidades, sinalizar itens esgotados e reservar/devolver unidades em pedidos/cancelamentos.
- Painel com resumo operacional, grafico de volume bruto de pedidos, itens mais pedidos e exportacao CSV sem dados de contato/endereco.
- A tela fiscal deixa explicito que nenhum documento e emitido enquanto nao houver provedor, cadastro fiscal e certificado configurados.
- Acompanhamento do pedido com mapa Leaflet/OpenStreetMap, rota consultada no OSRM e marcador de entregador animado ao longo do trajeto.
- Chat associado ao pedido e avaliacao disponivel apos a entrega.

## Organizacao

### Frontend — `src/`

- `App.jsx`: composicao da experiencia, navegacao, descoberta, filtros, autenticacao e sacola.
- `components/`: telas/componentes separados para catalogo, autenticacao, personalizacao, promocoes, sacola, mapa, pedidos, chat e avaliacoes.
- `services/api.js`: chamadas HTTP, autenticacao Bearer e contratos da API.
- `utils/format.js`: formatacao monetaria e rotulos de status.

### Backend — `backend/src/`

- `domain/` e `repository/`: entidades e acesso a dados com Spring Data JPA.
- `service/`: regras de autenticacao, catalogo, pedidos, promocoes e comunicacao.
- `security/`: BCrypt, tokens Bearer e regras de acesso por perfil.
- `api/`: controllers REST, DTOs e respostas de erro.
- `config/DemoDataInitializer.java`: contas, restaurantes e cardapios de demonstracao.
- `src/test/`: testes de integracao de autenticacao, catalogo e pedidos.

## API principal

Todos os caminhos abaixo sao relativos a `http://localhost:8080`. Endpoints autenticados usam `Authorization: Bearer <token>`.

| Metodo | Caminho | Acesso / finalidade |
|---|---|---|
| `GET` | `/api/health` | Publico; verificar a API |
| `GET` | `/api/catalog/summary` | Publico; total de restaurantes e itens |
| `GET` | `/api/restaurants?page=0&size=18` | Publico; restaurantes paginados, com filtros |
| `GET` | `/api/restaurants/{id}/menu?page=0&size=25` | Publico; cardapio paginado |
| `POST` | `/api/auth/register` | Publico; criar conta de cliente |
| `POST` | `/api/auth/login` | Publico; iniciar sessao |
| `GET` | `/api/auth/me` | Autenticado; consultar a conta atual |
| `PUT` | `/api/auth/me` | Autenticado; editar nome, e-mail, celular e endereco sugerido |
| `POST` | `/api/auth/logout` | Autenticado; revogar token |
| `GET` | `/api/promotions/banners` | Publico; banners de demonstracao |
| `GET` | `/api/promotions/surprise` | Publico; promocao-surpresa |
| `GET` | `/api/promotions/validate?code=...` | Publico; validar cupom |
| `POST` | `/api/orders` | Cliente autenticado; criar pedido |
| `GET` | `/api/orders` | Autenticado; listar pedidos da conta |
| `GET` | `/api/orders/{id}` | Dono do pedido ou administracao; acompanhar pedido |
| `GET` | `/api/orders/{id}/payment` | Dono do pedido ou operacao; transacao simulada |
| `POST` | `/api/orders/{id}/fiscal-document` | Dono do pedido ou operacao; comprovante demonstrativo sem validade fiscal |
| `GET/POST` | `/api/auth/me/payment-methods` | Cliente autenticado; lista/cadastra cartao demonstrativo (bandeira e quatro digitos apenas) |
| `DELETE` | `/api/auth/me/payment-methods/{id}` | Cliente autenticado; remove seu cartao demonstrativo |
| `GET` | `/api/admin/orders` | Operacao do restaurante ou administracao |
| `PATCH` | `/api/admin/orders/{id}/status` | Operacao do restaurante ou administracao; atualizar status |
| `GET` | `/api/admin/inventory?page=0&size=50` | Operacao do restaurante ou administracao; consultar estoque |
| `PATCH` | `/api/admin/inventory/{menuItemId}` | Operacao do restaurante ou administracao; atualizar unidades |
| `GET`, `POST` | `/api/chat/orders/{orderId}/messages` | Participante autorizado; listar/enviar mensagens |
| `GET` | `/api/reviews/orders/{orderId}` | Autenticado; consultar avaliacao |
| `POST` | `/api/reviews` | Dono de pedido entregue; enviar avaliacao |

O console do H2, disponivel em `http://localhost:8080/h2-console`, e apenas para desenvolvimento. JDBC URL: `jdbc:h2:mem:boradelivery`; usuario `sa`; senha vazia.

## Limites para uso real

- O catalogo e conteudo gerado para demonstracao, nao representa estabelecimentos ou disponibilidade reais.
- O catalogo nao usa nomes de restaurantes, cardapios, marcas ou imagens de estabelecimentos reais. Para importar comerciantes reais, use uma fonte licenciada/autorizada, valide a permissao de uso e mostre a atribuicao exigida; nao apresente dados de demonstracao como parceiros.
- O banco e em memoria: contas, pedidos, mensagens e avaliacoes desaparecem ao reiniciar. Uma operacao real requer banco persistente, backups e migracoes.
- O painel de restaurante e de demonstracao; contas de operacao compartilham permissao e ainda nao possuem vinculo por restaurante. Nao exponha publicamente sem revisar e endurecer esse modelo de autorizacao.
- Os pagamentos sao simulados: nao ha cobranca, autorizacao, estorno, cartoes reais/tokenizados, Pix dinamico, conciliacao ou integracao com BB Pay, NuPay, Alelo ou adquirente. O cadastro de cartao guarda apenas marca e quatro ultimos digitos para fins de interface, e nao e associado a cobrancas. As referencias `MOCK-*` nao podem ser usadas como comprovante de pagamento. Nao colete numero de cartao/CVV no Bora; implemente checkout hospedado/tokenizado por um PSP contratado.
- O relatorio financeiro resume valores de pedidos, nao saldo, lucro, comissao, repasse ou transacao bancaria. A exportacao CSV nao substitui conciliacao contabil.
- Nota fiscal nao e emitida: o endpoint so cria um comprovante marcado como sem validade fiscal. A escolha de NFS-e/NF-e depende da operacao e orientacao contabil; requer CNPJ e inscricoes, regime fiscal, certificado digital e integracao homologada com SEFAZ/municipio ou provedor fiscal.
- O perfil evita receber dados de pagamento, mas nome, e-mail, celular e endereco ainda exigem controles LGPD, politica de retencao, criptografia e governanca adequadas antes de usar dados reais.
- Em producao, configure `BORA_PII_ENCRYPTION_KEY` como chave Base64 de 32 bytes em um gerenciador de segredos antes de ativar o perfil Spring `prod`/`production`. Preserve e faca backup seguro dessa chave: sem ela, os campos criptografados ficam irrecuperaveis. A chave de desenvolvimento aleatoria nao deve ser usada com banco persistente.
- A criptografia de campos nao torna o projeto pronto para producao: faltam, entre outros, PostgreSQL/migracoes, cookies de sessao HttpOnly ou defesa equivalente contra XSS, rate limits, observabilidade, testes de autorizacao multi-restaurante e plano de rotacao de chaves.
- A rota e consultada no OSRM e o mapa usa OpenStreetMap. A posicao do entregador e interpolada como demonstracao; GPS real exige aplicativo de entregador, permissao, envio autenticado e tratamento de perda de sinal.
- O estoque e compartilhado no painel demonstrativo porque ainda nao existe associacao multi-restaurante entre contas de operacao e lojas. Ainda nao ha operacao real de restaurantes/entregadores, notificacoes push/SMS, antifraude, suporte operacional, gestao avancada de enderecos nem controles de producao. O mapa e o acompanhamento do entregador sao demonstrativos. Imagens, mapas e fontes dependem de servicos externos e internet.
