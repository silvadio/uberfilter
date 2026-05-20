# UberFilter — MVP

App Android nativo (Kotlin + Jetpack Compose) que lê o cartão de corrida da Uber Driver
via AccessibilityService e exibe um popup colorido indicando se a corrida é boa ou ruim.

---

## Estrutura do Projeto

```
UberFilter/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── res/
│       │   ├── values/strings.xml
│       │   └── xml/accessibility_service_config.xml
│       └── java/com/uberfilter/
│           ├── MainActivity.kt                     ← Tela principal + permissões + configurações
│           ├── model/
│           │   └── Models.kt                       ← RideOffer, FilterCriteria, RideEvaluation
│           ├── data/
│           │   └── FilterCriteriaStore.kt          ← Persistência com DataStore
│           ├── domain/
│           │   └── RideEvaluator.kt                ← Lógica de avaliação da corrida
│           ├── service/
│           │   ├── UberCardParser.kt               ← Extrai dados do cartão via regex
│           │   ├── UberAccessibilityService.kt     ← Escuta eventos da Uber Driver
│           │   ├── OverlayManager.kt               ← Exibe popup verde/vermelho
│           │   └── AppForegroundService.kt         ← Mantém o app vivo em background
│           ├── receiver/
│           │   └── BootReceiver.kt                 ← Reinicia após reboot
│           └── ui/
│               ├── SettingsViewModel.kt
│               └── theme/Theme.kt
├── gradle/
│   └── libs.versions.toml
├── build.gradle.kts
└── settings.gradle.kts
```

---

## Como rodar

1. **Abra no Android Studio** (Hedgehog ou superior)
2. Sincronize o Gradle
3. Instale no dispositivo (Android 8.0+ / API 26+)
4. Abra o app e conceda as duas permissões:
   - **"Exibir sobre outros apps"** → Configurações do sistema
   - **"Serviço de Acessibilidade"** → Ative "UberFilter – Leitor de Corridas"
5. Configure seus critérios e salve
6. Abra o app da Uber Driver — quando um cartão aparecer, o popup será exibido automaticamente

---

## Como funciona (fluxo)

```
Uber Driver abre cartão
        │
        ▼
UberAccessibilityService.onAccessibilityEvent()
        │
        ▼
UberCardParser.parse(rootNode)   ← varre a árvore de nós com regex
        │
        ▼
RideEvaluator.evaluate(offer, criteria)
        │
   ┌────┴────┐
   │         │
 BOA       RUIM
   │         │
   ▼         ▼
OverlayManager.show()  ← popup verde ou vermelho por 6 segundos
```

---

## Critérios configuráveis

| Critério                        | Padrão  |
|---------------------------------|---------|
| Valor mínimo total (R$)         | R$ 15   |
| Receita mínima por km (R$/km)   | R$ 2,00 |
| Avaliação mínima do passageiro  | 4,5 ⭐  |
| Distância máxima até passageiro | 3 km    |
| Tempo máximo até passageiro     | 8 min   |
| Distância mínima da viagem      | 3 km    |
| Duração máxima da viagem        | 40 min  |

---

## Permissões necessárias

| Permissão                    | Finalidade                              |
|------------------------------|-----------------------------------------|
| `SYSTEM_ALERT_WINDOW`        | Popup sobre outros apps                 |
| `BIND_ACCESSIBILITY_SERVICE` | Leitura dos nós da tela da Uber         |
| `FOREGROUND_SERVICE`         | Manter o serviço rodando em background  |
| `RECEIVE_BOOT_COMPLETED`     | Reiniciar após reboot                   |
| `POST_NOTIFICATIONS`         | Notificação do foreground service       |

---

## Próximos passos sugeridos

- [ ] Suporte a 99 e InDriver (criar parsers adicionais + ampliar `packageNames`)
- [ ] Histórico de corridas avaliadas
- [ ] Aceitar/recusar corrida direto pelo popup (acessibilidade performAction)
- [ ] Configuração de regiões bloqueadas (destinos ou origens)
- [ ] Widget de estatísticas (corridas aceitas/recusadas no dia)
