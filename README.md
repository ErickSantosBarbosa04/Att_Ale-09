# Juris Home — acesso seguro

Base de autenticação e autorização para o projeto PFC Juris Home. O sistema inclui cadastro, login, logout, três perfis de acesso, sessões armazenadas no MongoDB e páginas Thymeleaf com identidade visual própria.

## Estrutura do projeto

```text
juris-home-login-seguro/
├── backend/
│   └── src/main/
│       ├── java/br/com/jurishome/auth/
│       │   ├── bootstrap/       criação opcional do administrador inicial
│       │   ├── config/          segurança, sessões, tema e configuração
│       │   ├── user/            usuário, perfis, validação, repositório e serviço
│       │   └── web/             controladores e dados compartilhados das telas
│       └── resources/
│           └── application.properties
├── frontend/
│   └── src/main/resources/
│       ├── templates/           páginas e fragmentos Thymeleaf
│       │   ├── admin/
│       │   ├── areas/
│       │   ├── auth/
│       │   ├── errors/
│       │   └── fragments/
│       └── static/
│           ├── css/             estrutura visual e temas independentes
│           └── js/              interações locais da interface
├── pom.xml                      build Spring Boot com fontes das duas áreas
├── run-local.ps1                execução local no Windows
├── .env.example                 exemplo de variáveis sem credenciais reais
└── README.md
```

O **back-end** concentra regras, persistência, validação e autorização. O **front-end** contém templates, estilos e scripts. Eles ficam em diretórios separados e são reunidos pelo Maven no pacote Spring Boot para que o Thymeleaf renderize as páginas no servidor; portanto, não são dois serviços implantados separadamente.

## Requisitos

- Java 21
- Maven 3.9 ou superior
- MongoDB Atlas para execução com banco na nuvem; ou MongoDB Community para execução local

No pacote original de desenvolvimento, `runtime/` pode conter ferramentas locais baixadas. Essa pasta é ignorada pelo Git e não é necessária quando Java, Maven e MongoDB já estiverem instalados no ambiente.

## Executar no Windows

Com Java 21, Maven e MongoDB Community disponíveis no `PATH`, abra o PowerShell na pasta do projeto e execute:

```powershell
./run-local.ps1
```



Se o projeto estiver com os binários presentes em `runtime/`, o script prioriza o Java 21, o Maven e o MongoDB incluídos nessa pasta. Em outra máquina, instale os requisitos e use a configuração do `PATH`.

## Configurar o MongoDB Atlas

1. Crie um cluster no MongoDB Atlas e um usuário de banco dedicado à aplicação, com permissão `readWrite` no banco `juris_home_auth`.
2. Em **Network Access**, permita o endereço IP da máquina que executará o sistema. Evite liberar acesso irrestrito.
3. Copie a string de conexão do Atlas. Substitua usuário, senha e cluster; codifique caracteres reservados da senha para uso em URI.
4. Configure as variáveis no mesmo terminal que iniciará a aplicação. Não coloque credenciais reais em `application.properties`, `.env.example` ou no Git:

```powershell
$env:MONGODB_URI = "mongodb+srv://USUARIO:SENHA_CODIFICADA@SEU_CLUSTER.mongodb.net/juris_home_auth?retryWrites=true&w=majority"
$env:COOKIE_SECURE = "true"
$env:UI_THEME = "juris"
mvn -B -ntp spring-boot:run
```

Para criar o primeiro administrador, configure também os valores abaixo **antes da primeira inicialização**. Use uma senha forte e exclusiva. O serviço cria essa conta apenas se o usuário e o e-mail ainda não existirem.

```powershell
$env:INITIAL_ADMIN_USERNAME = "admin"
$env:INITIAL_ADMIN_EMAIL = "admin@seudominio.com"
$env:INITIAL_ADMIN_PASSWORD = "defina-uma-senha-forte-e-exclusiva"
```

As sessões são armazenadas na coleção `sessions` do MongoDB. Em produção, utilize HTTPS e mantenha `COOKIE_SECURE=true`.

## Perfis e autorização

| Perfil | Permissões principais |
| --- | --- |
| `CLIENTE` | Área do cliente (`/cliente/**`) |
| `ADVOGADO` | Área profissional (`/advogado/**`) |
| `ADMIN` | Administração de contas (`/admin/**`) e área profissional |

O cadastro público sempre cria um `CLIENTE`; não há campo para solicitar perfil privilegiado. A autorização é aplicada pelo Spring Security nas rotas e, na gestão administrativa, também por segurança de método. Alterar perfil ou bloquear uma conta invalida as sessões ativas dessa pessoa. O administrador não pode rebaixar, bloquear ou excluir a própria conta.

## Interface e temas

O tema visual padrão é `juris`, definido por `UI_THEME`. O tema `neutro` é uma alternativa de demonstração. Ambos usam os mesmos templates e as mesmas variáveis CSS semânticas.

- Ajuste tipografia, espaçamento e componentes comuns em `frontend/src/main/resources/static/css/app.css`.
- Ajuste cores do Juris Home em `frontend/src/main/resources/static/css/themes/juris.css`.
- Para adicionar outra identidade visual, crie um arquivo de tema com os mesmos tokens e permita seu nome em `UiProperties`.
- Atualize textos e estrutura das páginas em `frontend/src/main/resources/templates/`.
- Acrescente regras e operações em `backend/src/main/java/br/com/jurishome/auth/` sem colocar lógica de negócio nos templates.

O layout segue a identidade do portal Juris Home — azul profundo, tons claros e cobre — e compartilha cabeçalho, rodapé, formulários e componentes entre a página pública, autenticação, áreas por perfil e gestão administrativa.

## Gerar e iniciar o pacote

```powershell
mvn -DskipTests package
java -jar target/juris-home-login-seguro-1.0.0-exec.jar
```

Antes de iniciar o JAR, defina `MONGODB_URI` para o MongoDB desejado. A aplicação não inclui usuários nem senhas padrão. Para dados reais, configure também HTTPS, política de backup e controles operacionais do cluster.
