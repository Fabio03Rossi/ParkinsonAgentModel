# Treatment Agent Documentation

## 1. Ruolo e Obiettivi del Treatment Agent

### 1.1 Responsabilità Principale
Il **Treatment Agent** è un agente pasivo (ossia non si muove nello spazio fisico) che:
- **Percepisce** lo stato biologico della Substantia Nigra (popolazione neuronale)
- **Decide** una dose di farmaco (GLP-1) basata su una politica appresa (Q-learning)
- **Somministra** il farmaco modulando i parametri biologici dell'ambiente
- **Apprende** dai risultati attraverso il calcolo della reward e aggiornamento della Q-table

### 1.2 Ciclo Decisionale (Tick-by-Tick)
Ogni tick di simulazione (1 unità di tempo biologico):

```
Priority 3 (step):        Applica rateModifier ai parametri biologici basato su dose
          ↓
Priority 4 (stepQAction): Percepisci stato corrente → Decidi azione → Somministra dose
          ↓
Priority 5 (updateModel): Percepisci nuovo stato → Calcola reward → Aggiorna Q-table
          ↓
Check terminal state:     Se stationaryStep==50 o tick==1200 → Episodio finito
```

### 1.3 Metriche di Successo
- **Minimizzazione della morte neuronale** (target: ≤1 neurone degenerato)
- **Massimizzazione della salute media neuronale** (target: ~28/30 health units)
- **Efficienza della dose**: raggiungere salute target con dose minima (trade-off tossicità)
- **Robustezza**: funzionare su difficoltà variabili (curriculum learning)

---

## 2. Concetto di Policy di Environment

### 2.1 Cos'è la Policy
La **Policy** è un Singleton manager che centralizza e modifica **5 parametri biologici chiave** dell'ambiente Substantia Nigra:

| Parametro | Significato Biologico | Valore Base | Effetto |
|-----------|----------------------|------------|---------|
| **CYTO_RELEASE_RATE** | Tasso di rilascio citochine da microglia attivata | 0.5 | ↓ con dose (1/modifier) |
| **DEGENERATION_RATE** | Tasso di morte neuronale indotto da stress | 1.0 | ↓ con dose (1/modifier) |
| **CYTO_NEURON_THRESHOLD** | Soglia citochina che stresa un neurone | 5.0 | ↑ con dose (3.0×modifier) |
| **CYTO_MICROGLIA_THRESHOLD** | Soglia citochina che attiva una microglia | 3.0 | ↑ con dose (3.0×modifier) |
| **EVAPORATION_RATE** | Dissipazione del campo citochina | 0.05 | Variabile (1.0 - modifier/50) |

### 2.2 Architettura della Policy
```java
Policy policy = Policy.getInstance();  // Singleton

// Accesso ai parametri modificabili
policy.getParam(StatType.CYTO_RELEASE_RATE).setModifier(1 / rateModifier);
policy.getParam(StatType.DEGENERATION_RATE).setModifier(1 / rateModifier);
policy.getParam(StatType.CYTO_NEURON_THRESHOLD).setModifier(rateModifier * 3.0);
policy.getParam(StatType.CYTO_MICROGLIA_THRESHOLD).setModifier(rateModifier * 3.0);
policy.getParam(StatType.EVAPORATION_RATE).setModifier(1.0 - rateModifier / 50);

// Il valore effettivo = baseValue × modifier
double effectiveValue = policy.getParam(StatType.CYTO_RELEASE_RATE).getEffectiveValue();
```

### 2.3 Principio Biologico del Dosaggio
Il farmaco (GLP-1) **riduce l'infiammazione** attraverso:
1. **Inibizione del rilascio citochina** (↓ CYTO_RELEASE_RATE)
2. **Protezione neuronale** (↓ DEGENERATION_RATE, ↑ thresholds di soglia)
3. **Accelerazione della clearance** (↑ EVAPORATION_RATE con moderazione)

Il modifier è **funzione monotona crescente della dose**: dose↑ → modifier↑ → protection↑

### 2.4 Limitazioni e Design Constraints
- **Non modifica** direttamente le dinamiche di morte neuronale già iniziate
- **Tossicità** è penalizzata nella reward function (costo della dose)
- **Ritardo biologico** non è modellato (effetto istantaneo, assunzione semplificatrice)
- **Saturazione**: a dosi molto alte (>2.0), modifier non garantisce survival al 100%

---

## 3. Spazio degli Stati: Substantia Nigra State

### 3.1 Definizione dello Stato
Lo stato rappresenta la **percezione dell'agente** del livello di infiammazione nel compartimento neuronale.
È una discretizzazione 3D dello stato biologico continuo.

### 3.2 Dimensioni dello Stato
```
SubstanciaNigraState(degeneratedNeuronCount, stressedNeuronCount, inflammatedMicrogliaCount, 
                     actualDegenNeuron, averageNeuronHealth, currentGLP1dose)
```

| Dimensione | Intervallo | Granularità | Significato |
|-----------|-----------|-----------|-----------|
| **Dead neurons (DEG)** | [0, 31] | Discrete | Neuroni morti (stato irreversibile) |
| **Stressed neurons (STRESS)** | [0, 31] | Discrete | Neuroni a bassa salute (<30% max) |
| **Microglia inflammation (INFLAM)** | [0, 10] | Discrete (bucket aggregato) | Proxy dell'infiammazione sistemica |
| **Actual degeneration** | [0, 31] | Continuous | Health totale aggregata (logging) |
| **Average neuron health** | [0, 30] | Continuous | Salute media per analisi |
| **Current dose** | [0, 2.0] | Continuous | Dose somministrata (logging, non in hashCode) |

### 3.3 Discretizzazione dello Spazio degli Stati
```
Spazio dello stato Q-learning: DEG × STRESS × INFLAM
                             = 32 × 32 × 11 ≈ 11,264 stati possibili
                             
Spazio effettivo: ~7,700 stati
(molti stati fisicamente impossibili, es. DEG + STRESS > 31)
```

**Nota critica**: La dose `currentGLP1dose` è **esclusa intenzionalmente dal hashCode**:
- La dose è un'**azione**, non uno **stato**
- Lo stato deve essere invariante rispetto alla scelta di controllo
- Questo evita l'esplosione combinatoriale dello stato space

### 3.4 Codifica dello Stato per Q-Learning
```java
// hashCode mantiene la proprietà di unicità per stato biologico
// indipendente dalla dose
@Override
public int hashCode() {
    return degeneratedNeuronCount + 31 * stressedNeuronCount;
    // inflammatedMicrogliaCount già incluso nel calcolo discreto
}
```

### 3.5 Boundary Conditions e Stati Terminali
```
Stato iniziale (curriculum):
  - Easy (run 0-50):    1 dead, 0 stressed
  - Medium (run 51-100): 3 dead, 2 stressed
  - Hard (run 101+):     5 dead, 5 stressed

Condizioni terminali:
  1. Vittoria:    stationaryStep == 50 (inflam rimane bassa 50 ticks)
  2. Timeout:     tick == 1200 (episodio cancellato, costo-opportunità)
  
Sink state: Dead neurons == 31 (popolazione neuronale estinta)
```

### 3.6 Evoluzione dello Stato
Lo stato evolve secondo:
- **Percezione diretta**: Il Treatment agent conta neuroni nel Context
- **Frequenza**: Ogni tick (1 unità biologica)
- **Fonte**: Neuron population queries nel ParkinsonBuilder context

---

## 4. Spazio delle Azioni: Dosaggi Discreti

### 4.1 Definizione dell'Azione
Un'azione è una **dose di farmaco GLP-1** somministrata nel tick corrente.

### 4.2 Spazio delle Azioni Discretizzate
```
Actions = {0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0,
           1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 2.0}

|A| = 21 azioni discrete
MAX_DOSE = 2.0

Granularità: 0.1 unità di dose
```

### 4.3 Rappresentazione dell'Azione
```java
public class Dosage implements Action {
    private double dose;  // [0.0, 2.0]
    
    public Dosage(double dose) {
        this.dose = Math.max(0.0, Math.min(2.0, dose));  // Clamp
    }
}
```

### 4.4 Scelta dell'Azione: Politiche Alternative
Il sistema supporta **4 PolicyMode** per baseline comparison e ablation studies:

| PolicyMode | Formula | Significato | Use Case |
|-----------|---------|-----------|----------|
| **RL** | ε-greedy Q-learning | Politica appresa | Principale |
| **NO_DOSE** | dose = 0.0 | Nessun trattamento | Control baseline |
| **MAX_DOSE** | dose = 2.0 | Massima somministrazione | Upper bound biologico |
| **PROPORTIONAL** | dose = (deg + 0.5×stress)/tot × 2.0 | Euristica fissa | Baseline intelligente |

```java
public enum PolicyMode {
    RL,           // Q-learning epsilon-greedy
    NO_DOSE,      // Always 0.0
    MAX_DOSE,     // Always 2.0
    PROPORTIONAL  // Heuristic: dose ~ disease severity
}
```

### 4.5 Q-Learning Exploration-Exploitation
Per la politica **RL**, la scelta dell'azione segue **epsilon-greedy**:

```
ε-decay (dual scale):
  ε = max(0.05, 0.8 × 0.996^(batch_run) × 0.999^(tick))

Azione:
  Con probabilità ε:      Scegli azione casuale (exploration)
  Con probabilità (1-ε):  Scegli argmax_a Q(state, action) (exploitation)
```

**Interpretazione**:
- Nei batch run iniziali: ε alto (più exploration)
- Negli ultimi episodi del batch: ε basso (sfruttamento della politica)
- Minimo ε = 0.05 (sempre explore il 5% delle volte)

### 4.6 Sommministrazione della Dose
Una volta scelta l'azione (dose), questa viene:
1. **Memorizzata** come `currentGLP1dose`
2. **Trasformata** in `rateModifier` (vedi sezione 5)
3. **Applicata** ai 5 parametri biologici (vedi sezione 5)
4. **Loggata** nel CSV di output per analisi post-hoc

---

## 5. Mapping: Azione-Dosaggio → Modifica dei Parametri

### 5.1 Pipeline di Trasformazione
```
Azione (Dosage) → rateModifier → ModifiableParameter modifiers → Biological Parameters
```

### 5.2 Funzione di Trasformazione: Dose → rateModifier
Il dosaggio è **trasformato in un modificatore biologico** tramite una funzione piecewise:

```java
double rateModifier;
if (dose < 1.6) {
    // Regime conservativo (logaritmico)
    rateModifier = 1 + ln(1 + dose);
    // Esempi:
    //   dose=0.0 → modifier=1.00 (nessun effetto)
    //   dose=0.5 → modifier=1.40
    //   dose=1.0 → modifier=1.69
    //   dose=1.5 → modifier=1.84
} else {
    // Regime aggressivo (esponenziale)
    double baseValue = 1 + ln(1 + 1.6) ≈ 1.86;
    double excessDose = dose - 1.6;
    rateModifier = 1.86 + 2.5 × excessDose;
    // Esempi:
    //   dose=1.6 → modifier=1.86
    //   dose=1.8 → modifier=2.36
    //   dose=2.0 → modifier=2.86
}
```

**Razionale biologico**:
- **Dose basse** (< 1.6): effetto sublineare (curva di saturazione iniziale)
- **Dose alte** (≥ 1.6): effetto lineare accelerato (boost aggiuntivo per emergenze)

### 5.3 Applicazione ai Parametri Biologici

Una volta calcolato `rateModifier`, questo viene applicato ai 5 parametri:

#### 5.3.1 CYTO_RELEASE_RATE (inibizione del rilascio citochina)
```
Valore base:        0.5 citochina/tick
Modifica:           modifier = 1 / rateModifier
Effetto biologico:  Dose ↑ → rilascio citochina ↓

Esempi:
  dose=0.0  → 1/1.00=1.00 → CYTO_RELEASE_RATE = 0.5 (no change)
  dose=1.0  → 1/1.69=0.59 → CYTO_RELEASE_RATE = 0.30 (↓ 40%)
  dose=2.0  → 1/2.86=0.35 → CYTO_RELEASE_RATE = 0.18 (↓ 65%)
```

#### 5.3.2 DEGENERATION_RATE (protezione neuronale)
```
Valore base:        1.0 health_loss/tick
Modifica:           modifier = 1 / rateModifier
Effetto biologico:  Dose ↑ → morte neuronale ↓

Esempi:
  dose=1.0  → DEGENERATION_RATE = 0.59 (↓ 41%)
  dose=2.0  → DEGENERATION_RATE = 0.35 (↓ 65%)
```

#### 5.3.3 CYTO_NEURON_THRESHOLD (protezione da stress)
```
Valore base:        5.0 citochina
Modifica:           modifier = rateModifier × 3.0  [AMPLIFICAZIONE ×3.0]
Effetto biologico:  Dose ↑ → neuroni rimangono healthy più a lungo

Esempi:
  dose=0.0  → 1.00×3.0=3.00   → threshold=3.00 (base=5.0, DOWN 40%)
  dose=1.0  → 1.69×3.0=5.07   → threshold=5.07 (base=5.0, ~no change)
  dose=2.0  → 2.86×3.0=8.58   → threshold=8.58 (base=5.0, UP 71%)

Interpretazione: Con GLP-1, neuroni tollerano più citochina prima di stressarsi
```

#### 5.3.4 CYTO_MICROGLIA_THRESHOLD (freno all'attivazione microglia)
```
Valore base:        3.0 citochina
Modifica:           modifier = rateModifier × 3.0
Effetto biologico:  Dose ↑ → microglia meno reactive

Esempi:
  dose=1.0  → threshold=5.07 (base=3.0, UP 69%)
  dose=2.0  → threshold=8.58 (base=3.0, UP 186%)
```

#### 5.3.5 EVAPORATION_RATE (clearance del campo citochina)
```
Valore base:        0.05 (5% clearance/tick)
Modifica:           modifier = 1.0 - rateModifier / 50
Effetto biologico:  Moderato (non dipende fortemente da dose)

Esempi:
  dose=0.0  → 1.0 - 1.00/50 = 0.980 → EVAPORATION = 0.980×0.05 ≈ 0.049
  dose=1.0  → 1.0 - 1.69/50 = 0.966 → EVAPORATION ≈ 0.048
  dose=2.0  → 1.0 - 2.86/50 = 0.943 → EVAPORATION ≈ 0.047
```

### 5.4 Diagramma di Flusso Completo

```
┌─────────────────────────────────────────────────────────────┐
│  Treatment Agent Decision: Choose Action (Dosage)            │
│  ε-greedy Q-learning based on SubstanciaNigraState          │
└────────────────┬────────────────────────────────────────────┘
                 │
                 ↓
┌─────────────────────────────────────────────────────────────┐
│  Dose → rateModifier (Piecewise Function)                    │
│  ┌────────────────────────────────────────────────────────┐ │
│  │ dose < 1.6:  rateModifier = 1 + ln(1 + dose)          │ │
│  │ dose ≥ 1.6:  rateModifier = 1.86 + 2.5×(dose - 1.6)   │ │
│  └────────────────────────────────────────────────────────┘ │
└────────────────┬────────────────────────────────────────────┘
                 │
         ┌───────┴───────┬──────────────────┬─────────────┐
         │               │                  │             │
         ↓               ↓                  ↓             ↓
    ÷ rateModifier  ÷ rateModifier   × rateModifier×3.0  1.0 - modifier/50
         │               │                  │             │
         ↓               ↓                  ↓             ↓
  CYTO_RELEASE_RATE  DEGENERATION_RATE  CYTO_NEURON_    EVAPORATION_RATE
  (inibition)        (protection)       THRESHOLD
                                         CYTO_MICROGLIA_
                                         THRESHOLD
                                         (protection)
```

### 5.5 Effetti Biologici Integrati
```
Scenario: Microglia cascade è infiammata, neuroni stressati

Azione RL: dose = 1.2
  → rateModifier = 1 + ln(1 + 1.2) ≈ 1.74

Effetto:
  ✓ CYTO_RELEASE_RATE ÷ 1.74  → meno citochina prodotta
  ✓ DEGENERATION_RATE ÷ 1.74  → meno morte neuronale
  ✓ CYTO_NEURON_THRESHOLD ×5.22  → neuroni tollerano più stress
  ✓ CYTO_MICROGLIA_THRESHOLD ×5.22  → microglia meno reactive

Risultato integrato:
  1. Citochine già prodotte evaporano più lentamente (↓ EVAPORATION)
  2. Ma nuove citochine vengono prodotte meno (↓ RELEASE_RATE)
  3. Neuroni sono più resistenti (↑ thresholds)
  4. Cascata microglia si spegne più velocemente

→ Infiammazione iniziale viene contenuta, ma NON sempre fermata
```

### 5.6 Limitazioni del Mapping
1. **Dose alta non garantisce vittoria**: con cascata microglia già iniziata, anche dose=2.0 può perdere
2. **Nessun delay biologico**: modificatori sono applicati istantaneamente (semplificazione)
3. **Linearità nell'integrazione**: gli effetti non interagiscono non-linearmente
4. **Nessuna saturazione**: oltre dose=2.0 il modello non è calibrato

---

## 6. Ciclo di Apprendimento Reinforcement Learning (BONUS - se desiderato)

### 6.1 Q-Learning Update Rule
```
Q(s,a) ← Q(s,a) + α × [R(s,a,s') + γ × max_a' Q(s', a') - Q(s,a)]

Parametri:
  α (learning rate)     = 0.1
  γ (discount factor)   = 0.9
  R (reward function)   = somma pesata di componenti biologiche
```

### 6.2 Reward Function Shaping
```
totalReward = doseCost + deathPenalty + deathPenalty2 + trendPenalty 
            + doseBonus + improvementBonus

doseCost = -0.3 × (dose / 2.0)              [Penalizza sovra-dosaggio]
deathPenalty = -0.3 × (actualDeg / 31)     [Penalizza morte neuronale]
deathPenalty2 = -0.4 × (deg / 31)          [Penalizza dead count]
trendPenalty = -2.0 × max(0, ΔdeadCount)/31 [Penalizza peggioramento]
doseBonus = 0.8 × (inflam/31) × (dose/2.0) IF ΔdeadCount≤0  [Premia dose efficace]
improvementBonus = 0.5 IF ΔdeadCount==0 AND dose>0.2  [Premia stabilizzazione]
```

### 6.3 Terminal States e Episodio
```
Episodio termina SE:
  1. stationaryStep == 50  (vittoria: infiammazione stabile per 50 ticks)
  2. tick == 1200          (timeout: rinuncia)
  3. DEG == 31             (sconfitta: popolazione estinta)

Alla terminazione:
  → Q-table viene serializzata in JSON
  → Metriche episodio scritte in CSV
  → Nuovo episodio inizia con nuova stochasticity
```
---

## 7. Possibili aggiornamenti futuri

### 7.1 Sezioni Tecniche (Per Review Approfondita)
- [ ] **Implementazione Java**: Class diagram, metodi principali, sincronizzazione thread
- [ ] **Persistenza**: Come Q-table è serializzata/deserializzata, formato JSON
- [ ] **Data Logging**: Schema CSV, metriche tracciate, frecuenza sampling
- [ ] **Curriculum Learning**: Dinamica batch_run → difficoltà

### 7.2 Sezioni Scientifiche (Per Contributo Biologico)
- [ ] **Validazione biologica**: Confronto rateModifier con dati empirici GLP-1
- [ ] **Parametrizzazione**: Da dove vengono i 5 parametri base (letteratura / fit)
- [ ] **Sensibilità**: Sensitivity analysis sui coefficienti della reward function
- [ ] **Generalizzabilità**: Come il training trasferisce a diverse inizializzazioni

### 7.3 Sezioni Sperimentali (Per Reproducibility)
- [ ] **Baseline Comparison**: Risultati RL vs NO_DOSE vs PROPORTIONAL vs MAX_DOSE
- [ ] **Ablation Studies**: Effetto di variare singoli parametri della reward
- [ ] **Convergence Plot**: Q-table convergence rate, epsilon decay, episode reward trend
- [ ] **Policy Heatmap**: Visualizzazione della politica appresa (stato → argmax dose)

### 7.4 Considerazioni per Miglioramenti Futuri
- [ ] **Transfer Learning**: Policy pre-trained su difficoltà easy → transfer a hard
- [ ] **Continuous Actions**: Estensione da 21 dosaggi discreti a azioni continue
- [ ] **Multi-agent**: Trattamento collaborativo di multiple regioni cerebrali
- [ ] **Imitation Learning**: Warm-start Q-learning con esperto umano

---

## Riferimenti Interni
- **Treatment.java**: `src/parkinson/agent/passive/Treatment.java`
- **Policy.java**: `src/parkinson/Policy.java`
- **SubstanciaNigraState.java**: `src/parkinson/learning/SubstanciaNigraState.java`
- **TreatmentModel.java**: `src/parkinson/learning/TreatmentModel.java`
- **ParkinsonBuilder.java**: `src/parkinson/ParkinsonBuilder.java`
- **Risultati Baseline**: `outputKami/outputKamirlConvergence_{RL,NODOSE,MAXDOSE,PROPORTIONAL}.csv`
