# API Interclasse ETEC

API REST para organizar o **interclasse de final de ano** da ETEC.
Projeto educacional: a base está pronta (estrutura MVC, banco SQLite e login),
e o restante do sistema é implementado pelos alunos a partir das atividades deste README.

---

## 1. Como rodar

**Requisitos:** JDK 17+, Maven 3.8+

```bash
# sobe o Tomcat 7 embutido na porta 8082
mvn tomcat7:run
```

A API fica em `http://localhost:8082/api/`.

> O banco `interclasse.db` (SQLite) é criado automaticamente na primeira conexão,
> junto com as tabelas e os dados iniciais.

**Teste rápido:** `GET http://localhost:8082/api/` (com a barra no final)

> O `interclasse.db` é criado na pasta do projeto. Para começar do zero, apague o arquivo
> e suba o servidor de novo.

### Testando com curl

> **Windows (PowerShell):** use `curl.exe` (com `.exe`), porque `curl` sozinho é um atalho para outro comando.
> O `--%` logo depois evita que o PowerShell estrague as aspas do JSON, que ficam escapadas com `\"`.
> No Linux/Mac use `curl` normal, sem o `--%`.
>
> Quase todas as rotas exigem login. Só `/api/` e `/api/auth/*` são livres.

```powershell
# cadastra participante
curl.exe --% -X POST http://localhost:8082/api/auth/registrar -H "Content-Type: application/json" -d "{\"nome\":\"Ana Souza\",\"email\":\"ana@etec.sp.gov.br\",\"senha\":\"123456\"}"

# cadastra organizador (precisa da chave mestra)
curl.exe --% -X POST http://localhost:8082/api/auth/registrar-organizador -H "Content-Type: application/json" -d "{\"nome\":\"Prof Carlos\",\"email\":\"carlos@etec.sp.gov.br\",\"senha\":\"123456\",\"chaveMestra\":\"ETEC-INTERCLASSE-2026\"}"

# login (use -c cookies.txt para manter a sessao)
curl.exe --% -X POST http://localhost:8082/api/auth/login -H "Content-Type: application/json" -c cookies.txt -d "{\"email\":\"carlos@etec.sp.gov.br\",\"senha\":\"123456\"}"

# cria um time (usa a sessao salva com -b cookies.txt)
curl.exe --% -X POST http://localhost:8082/api/times -H "Content-Type: application/json" -b cookies.txt -d "{\"nome\":\"Falcao da Silva\",\"cor\":\"Azul\",\"responsavel\":\"Carlos\",\"contato\":\"11988887777\"}"
```

**Postman/Insomnia:** importe a coleção ou use as URLs acima. Lembre-se de ativar
`Cookie Manager` / `withCredentials` para as rotas que exigem login.

---

## 2. Estrutura (MVC)

```
src/main/java/com/example/
├── controller/     -> Servlets (rotas HTTP). Recebem a requisição, chamam o DAO e devolvem JSON
│   ├── ApiController.java
│   ├── AuthController.java
│   └── TimeController.java
├── dao/            -> Acesso ao banco (SQL). Um DAO por entidade
│   ├── TimeDao.java
│   └── UsuarioDao.java
├── model/          -> Entidades (POJOs). Representam as tabelas
│   ├── Time.java
│   └── Usuario.java
├── filter/         -> Filtros de acesso (autenticacao/autorizacao)
│   └── OrganizadorFilter.java
└── util/           -> Utilitarios (conexao, JSON, sessao)
    ├── Conexao.java
    ├── JsonUtil.java
    └── SessaoUtil.java
```

**Fluxo de uma requisição:**
```
Request -> Filter (permissao) -> Controller (valida, orquestra) -> DAO (SQL) -> Model
        <- Controller monta o JSON <- DAO devolve os dados <-
```

---

## 3. Sistema de Login

Simples, baseado em **sessão** (`HttpSession`). Sem token, sem JWT.

| Tipo | Como se cadastra |
|---|---|
| `PARTICIPANTE` | Cadastro livre em `/api/auth/registrar` |
| `ORGANIZADOR` | Cadastro em `/api/auth/registrar-organizador`, **exige chave mestra** |

A chave mestra padrão é `ETEC-INTERCLASSE-2026` (definida na tabela `chave_mestra`).
Para gerar outras chaves, insira novas linhas na tabela pelo `sqlite3` ou pelo
método `UsuarioDao.cadastrarChave(String)`.

**Sessão:** após o login, o `usuarioId` e o `tipo` ficam guardados na sessão.
O `OrganizadorFilter` bloqueia as rotas `/api/admin/*` para quem não for organizador.

---

## 4. Entidades do sistema

O que **já existe** na base (`Conexao.criarTabelas`):

| Entidade | Tabela | Campos |
|---|---|---|
| `Usuario` | `usuario` | `id`, `nome`, `email`, `senha` (hash), `tipo`, `criado_em` |
| `Time` | `time` | `id`, `nome`, `cor`, `responsavel`, `contato`, `criado_em` |

O que os alunos **precisam criar** (Atividade 1 do README):

| Entidade | Tabela | Campos |
|---|---|---|
| `Turma` | `turma` | `id`, `nome` (ex: "2º DS"), `curso`, `ano` |
| `Modalidade` | `modalidade` | `id`, `nome` (FUTSAL, VOLEI, QUEIMADA...), `ativo` (0/1) |
| `Inscricao` | `inscricao` | `id`, `time_id`, `modalidade_id`, `data_inscricao`, `status` |
| `Partida` | `partida` | `id`, `modalidade_id`, `time_a_id`, `time_b_id`, `placar_a`, `placar_b`, `data`, `hora`, `local`, `fase`, `status` |
| `Configuracao` | `configuracao` | `chave`, `valor` (pontos e regras do campeonato) |

### Status e valores permitidos

| Campo | Valores |
|---|---|
| `usuario.tipo` | `PARTICIPANTE`, `ORGANIZADOR` |
| `inscricao.status` | `PENDENTE`, `CONFIRMADA`, `NEGADA` |
| `partida.status` | `AGENDADA`, `EM_ANDAMENTO`, `FINALIZADA`, `CANCELADA` |
| `partida.fase` | `AVULSO` (no MVP), depois `GRUPOS`, `QUARTAS`, `SEMI`, `FINAL` |

---

## 5. Regras de negócio

### 5.1 Inscrição
- Um time participa de **no máximo 1 modalidade por vez** (1 time por modalidade).
- A inscrição nasce como `PENDENTE`; só o organizador confirma (`CONFIRMADA`) ou nega (`NEGADA`).
- Só times com inscrição `CONFIRMADA` podem entrar em partida.

### 5.2 Partida
- Só pode ser criada entre dois times **confirmados na mesma modalidade**.
- Os dois times devem ser **diferentes**.
- O **placar só pode ser alterado** enquanto a partida não estiver `FINALIZADA`.
- Ao finalizar, o resultado alimenta a classificação.

### 5.3 Pontuação (configurável)
Os valores ficam na tabela `configuracao` e podem ser alterados sem mexer no código:

| Chave | Padrão | Significado |
|---|---|---|
| `PONTOS_VITORIA` | `3` | Pontos do vencedor |
| `PONTOS_EMPATE` | `1` | Pontos de cada time empatado |
| `PONTOS_DERROTA` | `0` | Pontos do perdedor |
| `PERMITE_EMPATE` | `1` | `1` = empate vale, `0` = não há empate |

> **Empate configurável:** se `PERMITE_EMPATE = 0`, a partida que terminar empatada
> não pode ser finalizada (retornar erro). Se for `1`, cada time leva `PONTOS_EMPATE`.

### 5.4 Classificação
Calculada a partir das partidas `FINALIZADAS` de uma modalidade:
1. `3` pontos pro vencedor, `1` pra cada um no empate, `0` pro perdedor.
2. Total de pontos.
3. Desempate 1: número de vitórias.
4. Desempate 2: saldo de gols (`placar_a + placar_b` pró - contra).
5. Desempate 3: nome do time (ordem alfabética).

---

## 6. Endpoints

### Auth
| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `POST` | `/api/auth/registrar` | livre | Cadastra participante |
| `POST` | `/api/auth/registrar-organizador` | chave mestra | Cadastra organizador |
| `POST` | `/api/auth/login` | livre | Faz login (cria sessão) |
| `POST` | `/api/auth/logout` | logado | Encerra a sessão |
| `GET` | `/api/auth/me` | logado | Dados do usuário logado |

**Body do cadastro/login:**
```json
{
  "nome": "Ana Souza",
  "email": "ana@etec.sp.gov.br",
  "senha": "123456",
  "chaveMestra": "ETEC-INTERCLASSE-2026"
}
```
(`chaveMestra` só em `/registrar-organizador`)

### Times (exemplo já implementado)
| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/times` | Lista todos os times |
| `GET` | `/api/times?id=1` | Detalhe de um time |
| `POST` | `/api/times` | Cria um time |
| `PUT` | `/api/times?id=1` | Atualiza um time |
| `DELETE` | `/api/times?id=1` | Remove um time |

### A implementar
| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `GET/POST` | `/api/turmas` | logado | Turmas |
| `GET/POST` | `/api/modalidades` | logado | Modalidades |
| `GET/POST` | `/api/inscricoes` | logado | Listar / Inscrever time |
| `PUT` | `/api/inscricoes?id=1` | organizador | Confirmar ou negar inscrição |
| `GET/POST` | `/api/partidas` | logado | Listar / Agendar partida |
| `PATCH` | `/api/partidas?id=1` | organizador | Atualizar placar |
| `POST` | `/api/partidas?id=1/finalizar` | organizador | Finalizar partida |
| `GET` | `/api/classificacao?modalidadeId=1` | logado | Tabela de classificação |

---

## 7. Lista de Atividades

Cada atividade é um pedaço do sistema. Sugestão: uma dupla por atividade.

### Fase 1 — Fundamentos — Vitoria
- [X] **1. Criar as tabelas restantes** (`turma`, `modalidade`, `inscricao`, `partida`, `configuracao`)
      com as chaves estrangeiras apontando para as tabelas existentes.
      Dica: adicione o `CREATE TABLE` dentro de `Conexao.criarTabelas`.
- [X] **2. Criar os Models** `Turma`, `Modalidade`, `Inscricao`, `Partida`, `Configuracao`
      (use `Time` e `Usuario` como modelo).
- [X] **3. Criar os DAOs** correspondentes, com os 5 métodos CRUD
      (veja `TimeDao` como referência).

### Fase 2 — CRUDs e rotas - Duda
- [X] **4. `TurmaController`** — `/api/turmas` (GET, POST, PUT, DELETE)
- [X] **5. `ModalidadeController`** — `/api/modalidades` (GET, POST, PUT, DELETE),
      incluindo ativar/desativar a modalidade.
- [X] **6. `InscricaoController`** — `/api/inscricoes`:
      - listar todas e listar por modalidade (`?modalidadeId=`)
      - inscribed time (com validação de **1 time por modalidade**)
      - alterar status (`PENDENTE` → `CONFIRMADA`/`NEGADA`), somente organizador
- [X] **7. `PartidaController`** — `/api/partidas`:
      - listar com filtros (`?modalidadeId=`, `?status=`, `?data=`)
      - agendar partida, validando: mesma modalidade, times confirmados, times diferentes
      - atualizar placar (só se não estiver `FINALIZADA`)
      - finalizar partida

### Fase 3 — Regras avançadas — Jolea
- [X] **8. `ConfiguracaoDao` + rota** — ler e alterar `PONTOS_VITORIA`, `PONTOS_EMPATE`,
      `PONTOS_DERROTA`, `PERMITE_EMPATE`.
- [X] **9. `ClassificacaoController`** — `/api/classificacao?modalidadeId=1`:
      calcula pontos, vitórias, saldo e ordena conforme a seção 5.4.
- [X] **10. Validar empate** — se `PERMITE_EMPATE = 0`, bloquear a finalização de partida empatada.
- [X] **11. Proteger as rotas de escrita** — mover o CRUD de organizers para `/api/admin/*`
      (já existe o `OrganizadorFilter`) e deixar as rotas de leitura livres para logados.

### Fase 4 — Acabamento — Jolea
- [X] **12. Validações** — nome obrigatório, email válido, campos não vazios,
      mensagens de erro claras em JSON.
- [X] **13. Documentar** — documentar todas as rotas no README e adicionar exemplos
      de requisição/resposta (curl ou Postman). - Duda
- [X] **14. Testes manuais** — checklist de teste de cada rota (entrada válida, entrada
      inválida, sem permissão, recurso inexistente). - Duda

### Bônus
- [ ] **15.** Chaveamento: grupos + mata-mata (`fase` = `GRUPOS`, `QUARTAS`, `SEMI`, `FINAL`).
- [X] **16.** Ranking individual (gols/assistências dos jogadores). - Duda
- [ ] **17.** Exportar a classificação em CSV.

---

## 8. Convenções

- **Resposta de sucesso:** o objeto JSON direto (`200`).
- **Resposta de erro:** `{"erro": "mensagem"}` com o status HTTP correto
  (`400` requisição inválida, `401` sem login, `403` sem permissão, `404` não encontrado, `409` conflito).
- **Datas:** texto no formato `yyyy-MM-dd` (`2026-11-20`).
- **SQL:** sempre `PreparedStatement`, nunca concatenar string.
- **Controller:** valida a entrada, chama o DAO e monta a resposta. Sem SQL na controller.

## 9. Testes manuais

Todos os comandos usam o `curl.exe` no **PowerShell do Windows**. O `--%` logo depois do `curl.exe` evita que o PowerShell estrague as aspas do JSON. No CMD, Linux ou Mac, tire o `--%` e use `curl`.

Base da URL: `http://localhost:8082/api` (subir com `mvn tomcat7:run`).
O `-c cookies.txt` salva a sessão e o `-b cookies.txt` usa a sessão salva.

> Os ids (1, 2...) valem para um banco novo. Para começar do zero, pare o servidor, apague o `interclasse.db` e suba de novo.

### 9.1 Preparação

```powershell
# Teste rápido da API (use a barra no final)
curl.exe -i http://localhost:8082/api/

# Cadastra organizador
curl.exe --% -X POST http://localhost:8082/api/auth/registrar-organizador -H "Content-Type: application/json" -d "{\"nome\":\"Prof Salomao\",\"email\":\"salomao@etec.sp.gov.br\",\"senha\":\"123456\",\"chaveMestra\":\"ETEC-INTERCLASSE-2026\"}"

# Login (salva a sessão)
curl.exe --% -X POST http://localhost:8082/api/auth/login -H "Content-Type: application/json" -c cookies.txt -d "{\"email\":\"salomao@etec.sp.gov.br\",\"senha\":\"123456\"}"

# Confere o usuário logado
curl.exe --% http://localhost:8082/api/auth/me -b cookies.txt
```

### 9.2 Fluxo completo (caminho feliz)

```powershell
# 1. Modalidade e times
curl.exe --% -X POST http://localhost:8082/api/modalidades -H "Content-Type: application/json" -b cookies.txt -d "{\"nome\":\"FUTSAL\"}"
curl.exe --% -X POST http://localhost:8082/api/times -H "Content-Type: application/json" -b cookies.txt -d "{\"nome\":\"Time A\"}"
curl.exe --% -X POST http://localhost:8082/api/times -H "Content-Type: application/json" -b cookies.txt -d "{\"nome\":\"Time B\"}"

# 2. Inscrições (nascem PENDENTE)
curl.exe --% -X POST http://localhost:8082/api/inscricoes -H "Content-Type: application/json" -b cookies.txt -d "{\"timeId\":1,\"modalidadeId\":1}"
curl.exe --% -X POST http://localhost:8082/api/inscricoes -H "Content-Type: application/json" -b cookies.txt -d "{\"timeId\":2,\"modalidadeId\":1}"

# 3. Organizador confirma
curl.exe --% -X PUT "http://localhost:8082/api/inscricoes?id=1" -H "Content-Type: application/json" -b cookies.txt -d "{\"status\":\"CONFIRMADA\"}"
curl.exe --% -X PUT "http://localhost:8082/api/inscricoes?id=2" -H "Content-Type: application/json" -b cookies.txt -d "{\"status\":\"CONFIRMADA\"}"

# 4. Agenda a partida, atualiza o placar e finaliza
curl.exe --% -X POST http://localhost:8082/api/partidas -H "Content-Type: application/json" -b cookies.txt -d "{\"modalidadeId\":1,\"timeAId\":1,\"timeBId\":2}"
curl.exe --% -X PUT "http://localhost:8082/api/partidas?id=1" -H "Content-Type: application/json" -b cookies.txt -d "{\"placarA\":2,\"placarB\":1}"
curl.exe --% -X POST "http://localhost:8082/api/partidas?id=1&acao=finalizar" -b cookies.txt

# 5. Classificação
curl.exe --% "http://localhost:8082/api/classificacao?modalidadeId=1" -b cookies.txt
```

**Resultado esperado:** o Time A aparece em 1º com 3 pontos e o Time B em 2º com 0 ponto.

### 9.3 Checklist por rota

Legenda: entrada válida, entrada inválida, sem permissão, recurso inexistente.

#### Auth
| Teste | Requisição | Esperado |
|---|---|---|
| Entrada válida | `POST /auth/registrar-organizador` com a chave certa | 200, `tipo: ORGANIZADOR` |
| Chave errada | mesma rota com `chaveMestra` errada | 403 `chave mestra invalida` |
| Campos faltando | `POST /auth/registrar` com corpo `{}` | 400 `nome, email e senha sao obrigatorios` |
| Email repetido | cadastrar o mesmo email duas vezes | 409 `email ja cadastrado` |
| Login válido | `POST /auth/login` | 200 e cookie salvo |
| Senha errada | `POST /auth/login` com senha errada | 401 `email ou senha invalidos` |
| Sem login | `GET /auth/me` sem `-b cookies.txt` | 401 |

#### Times, Turmas, Modalidades (CRUD)
| Teste | Requisição | Esperado |
|---|---|---|
| Criar válido | `POST` com os campos obrigatórios | 200 com `id` |
| Criar inválido | `POST` com corpo `{}` | 400 (campo obrigatório) |
| Buscar existente | `GET ...?id=1` | 200 |
| Buscar inexistente | `GET ...?id=999` | 404 |
| Id inválido | `GET ...?id=abc` | 400 `id invalido` |
| Atualizar | `PUT ...?id=1` com corpo válido | 200 |
| Atualizar inexistente | `PUT ...?id=999` | 404 |
| Remover | `DELETE ...?id=1` | 200 |
| Remover inexistente | `DELETE ...?id=999` | 404 |
| Sem login | qualquer rota sem cookie | 401 `faca login para continuar` |
| Sem permissão | `POST`/`PUT`/`DELETE` logado como participante | 403 `acesso restrito a organizadores` |

Campos obrigatórios: Time e Modalidade pedem `nome`; Turma pede `nome`, `curso` e `ano`.

#### Modalidade: ativar e desativar
```powershell
curl.exe --% -X PUT "http://localhost:8082/api/modalidades?id=1" -H "Content-Type: application/json" -b cookies.txt -d "{\"nome\":\"FUTSAL\",\"ativo\":0}"
```
Esperado: 200 com `ativo: 0`. Com `"ativo":5`, esperado 400 `ativo deve ser 0 ou 1`.

#### Inscrições
| Teste | Requisição | Esperado |
|---|---|---|
| Inscrever válido | `POST` com `timeId` e `modalidadeId` | 200, `status: PENDENTE` |
| Campos faltando | `POST` com corpo `{}` | 400 |
| Time já inscrito | `POST` repetido para o mesmo time | 409 `Time ja esta inscrito em uma modalidade` |
| Participante se inscreve | `POST` logado como participante | 200 (única escrita liberada para participante) |
| Confirmar | `PUT ?id=1` com `{"status":"CONFIRMADA"}` | 200 |
| Status inválido | `PUT ?id=1` com `{"status":"XPTO"}` | 400 `status deve ser CONFIRMADA ou NEGADA` |
| Inscrição inexistente | `PUT ?id=999` | 404 |
| Filtrar por modalidade | `GET ?modalidadeId=1` | 200 com a lista |

#### Partidas
| Teste | Requisição | Esperado |
|---|---|---|
| Agendar válida | `POST` com times confirmados | 200, `status: AGENDADA` |
| Times iguais | `POST` com `timeAId` = `timeBId` | 400 `Os times devem ser diferentes` |
| Time não confirmado | `POST` com time `PENDENTE` | 409 `Os dois times precisam estar confirmados na mesma modalidade` |
| Atualizar placar | `PUT ?id=1` com `placarA`/`placarB` | 200 |
| Placar negativo | `PUT` com `"placarA":-1` | 400 |
| Finalizar | `POST ?id=1&acao=finalizar` | 200, `status: FINALIZADA` |
| Finalizar de novo | mesmo comando | 409 `Partida ja esta finalizada` |
| Placar após finalizar | `PUT ?id=1` depois de finalizada | 409 `Partida finalizada nao pode ter o placar alterado` |
| Filtros | `GET ?status=AGENDADA` ou `?data=2026-11-20` | 200 só com as que combinam |
| Partida inexistente | `GET ?id=999` | 404 |

#### Configuração e classificação
```powershell
# lista as regras de pontuação
curl.exe --% http://localhost:8082/api/configuracao -b cookies.txt

# muda os pontos da vitória para 2
curl.exe --% -X PUT "http://localhost:8082/api/configuracao?chave=PONTOS_VITORIA" -H "Content-Type: application/json" -b cookies.txt -d "{\"valor\":\"2\"}"

# classificação de uma modalidade
curl.exe --% "http://localhost:8082/api/classificacao?modalidadeId=1" -b cookies.txt
```
| Teste | Esperado |
|---|---|
| `PUT` com valor `abc` | 400 `valor deve ser um numero inteiro` |
| `PUT` com valor `-1` | 400 `valor nao pode ser negativo` |
| `PUT ?chave=PERMITE_EMPATE` com valor `5` | 400 `PERMITE_EMPATE deve ser 0 ou 1` |
| `PUT ?chave=XYZ` | 404 `Configuracao nao encontrada` |
| `GET /classificacao` sem `modalidadeId` | 400 `modalidadeId e obrigatorio` |

#### Jogadores e ranking (bônus 16)
```powershell
curl.exe --% -X POST http://localhost:8082/api/jogadores -H "Content-Type: application/json" -b cookies.txt -d "{\"nome\":\"Ana\",\"timeId\":1,\"gols\":5,\"assistencias\":2}"
curl.exe --% -X POST http://localhost:8082/api/jogadores -H "Content-Type: application/json" -b cookies.txt -d "{\"nome\":\"Bia\",\"timeId\":1,\"gols\":3,\"assistencias\":7}"
curl.exe --% "http://localhost:8082/api/ranking?tipo=gols" -b cookies.txt
curl.exe --% "http://localhost:8082/api/ranking?tipo=assistencias" -b cookies.txt
```
Esperado: no ranking de gols a Ana vem na frente (5 gols), e no de assistências a Bia (7). Com `?tipo=xyz` o esperado é 400.

#### Teste de permissão (participante)
```powershell
# Cadastra participante e faz login com outro arquivo de cookie
curl.exe --% -X POST http://localhost:8082/api/auth/registrar -H "Content-Type: application/json" -d "{\"nome\":\"Ana Souza\",\"email\":\"ana@etec.sp.gov.br\",\"senha\":\"123456\"}"
curl.exe --% -X POST http://localhost:8082/api/auth/login -H "Content-Type: application/json" -c cookies-part.txt -d "{\"email\":\"ana@etec.sp.gov.br\",\"senha\":\"123456\"}"

# Participante lê (deve funcionar) e tenta criar (deve ser barrado)
curl.exe --% http://localhost:8082/api/times -b cookies-part.txt
curl.exe --% -X POST http://localhost:8082/api/times -H "Content-Type: application/json" -b cookies-part.txt -d "{\"nome\":\"Time C\"}"
```
Esperado: o `GET` responde 200 e o `POST` responde 403 `acesso restrito a organizadores`.

### 9.4 Problemas conhecidos / observações
- Rotas protegidas exigem login. Sem cookie a resposta é `401 faca login para continuar`.
- Mensagens `GRAVE ... module-info.class` no terminal do Tomcat 7 são inofensivas.
- JSON malformado (ou aspas estragadas pelo PowerShell) devolve 500 em vez de 400.
- Para finalizar uma partida use `POST /api/partidas?id=1&acao=finalizar`.
