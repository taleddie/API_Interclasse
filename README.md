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

A API fica em `http://localhost:8082/meu-servlet/api`.

> O banco `interclasse.db` (SQLite) é criado automaticamente na primeira conexão,
> junto com as tabelas e os dados iniciais.

**Teste rápido:** `GET http://localhost:8082/meu-servlet/api`

> O `interclasse.db` é criado na pasta do projeto. Para começar do zero, apague o arquivo
> e suba o servidor de novo.

### Testando com curl

```bash
# cadastra participante
curl -X POST http://localhost:8082/meu-servlet/api/auth/registrar -H "Content-Type: application/json" -d '{"nome":"Ana Souza","email":"ana@etec.sp.gov.br","senha":"123456"}'

# cadastra organizador (precisa da chave mestra)
curl -X POST http://localhost:8082/meu-servlet/api/auth/registrar-organizador -H "Content-Type: application/json" -d '{"nome":"Prof Carlos","email":"carlos@etec.sp.gov.br","senha":"123456","chaveMestra":"ETEC-INTERCLASSE-2026"}'

# login (use -c cookies.txt para manter a sessao)
curl -X POST http://localhost:8082/meu-servlet/api/auth/login -H "Content-Type: application/json" -c cookies.txt -d '{"email":"carlos@etec.sp.gov.br","senha":"123456"}'

# cria um time
curl -X POST http://localhost:8082/meu-servlet/api/times -H "Content-Type: application/json" -b cookies.txt -d '{"nome":"Falcao da Silva","cor":"Azul","responsavel":"Carlos","contato":"11988887777"}'
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

### Fase 1 — Fundamentos — Duda
- [ ] **1. Criar as tabelas restantes** (`turma`, `modalidade`, `inscricao`, `partida`, `configuracao`)
      com as chaves estrangeiras apontando para as tabelas existentes.
      Dica: adicione o `CREATE TABLE` dentro de `Conexao.criarTabelas`.
- [ ] **2. Criar os Models** `Turma`, `Modalidade`, `Inscricao`, `Partida`, `Configuracao`
      (use `Time` e `Usuario` como modelo).
- [ ] **3. Criar os DAOs** correspondentes, com os 5 métodos CRUD
      (veja `TimeDao` como referência).

### Fase 2 — CRUDs e rotas - Vitoria
- [ ] **4. `TurmaController`** — `/api/turmas` (GET, POST, PUT, DELETE)
- [ ] **5. `ModalidadeController`** — `/api/modalidades` (GET, POST, PUT, DELETE),
      incluindo ativar/desativar a modalidade.
- [ ] **6. `InscricaoController`** — `/api/inscricoes`:
      - listar todas e listar por modalidade (`?modalidadeId=`)
      - inscribed time (com validação de **1 time por modalidade**)
      - alterar status (`PENDENTE` → `CONFIRMADA`/`NEGADA`), somente organizador
- [ ] **7. `PartidaController`** — `/api/partidas`:
      - listar com filtros (`?modalidadeId=`, `?status=`, `?data=`)
      - agendar partida, validando: mesma modalidade, times confirmados, times diferentes
      - atualizar placar (só se não estiver `FINALIZADA`)
      - finalizar partida

### Fase 3 — Regras avançadas — Jolea
- [ ] **8. `ConfiguracaoDao` + rota** — ler e alterar `PONTOS_VITORIA`, `PONTOS_EMPATE`,
      `PONTOS_DERROTA`, `PERMITE_EMPATE`.
- [ ] **9. `ClassificacaoController`** — `/api/classificacao?modalidadeId=1`:
      calcula pontos, vitórias, saldo e ordena conforme a seção 5.4.
- [ ] **10. Validar empate** — se `PERMITE_EMPATE = 0`, bloquear a finalização de partida empatada.
- [ ] **11. Proteger as rotas de escrita** — mover o CRUD de organizers para `/api/admin/*`
      (já existe o `OrganizadorFilter`) e deixar as rotas de leitura livres para logados.

### Fase 4 — Acabamento — Jolea
- [ ] **12. Validações** — nome obrigatório, email válido, campos não vazios,
      mensagens de erro claras em JSON.
- [ ] **13. Documentar** — documentar todas as rotas no README e adicionar exemplos
      de requisição/resposta (curl ou Postman).
- [ ] **14. Testes manuais** — checklist de teste de cada rota (entrada válida, entrada
      inválida, sem permissão, recurso inexistente).

### Bônus
- [ ] **15.** Chaveamento: grupos + mata-mata (`fase` = `GRUPOS`, `QUARTAS`, `SEMI`, `FINAL`).
- [ ] **16.** Ranking individual (gols/assistências dos jogadores).
- [ ] **17.** Exportar a classificação em CSV.

---

## 8. Convenções

- **Resposta de sucesso:** o objeto JSON direto (`200`).
- **Resposta de erro:** `{"erro": "mensagem"}` com o status HTTP correto
  (`400` requisição inválida, `401` sem login, `403` sem permissão, `404` não encontrado, `409` conflito).
- **Datas:** texto no formato `yyyy-MM-dd` (`2026-11-20`).
- **SQL:** sempre `PreparedStatement`, nunca concatenar string.
- **Controller:** valida a entrada, chama o DAO e monta a resposta. Sem SQL na controller.
