# Treatment Agent Documentation (English)

## 1. Role and Objectives of the Treatment Agent

### 1.1 Primary Responsibilities
The **Treatment Agent** is a passive agent (i.e., does not move in physical space) that:
- **Perceives** the biological state of the Substantia Nigra (neuronal population)
- **Decides** a drug dose (GLP-1) based on a learned policy (Q-learning)
- **Administers** the drug by modulating the environment's biological parameters
- **Learns** from results through reward calculation and Q-table updates

### 1.2 Decision Cycle (Tick-by-Tick)
Each simulation tick (1 unit of biological time):

```
Priority 3 (step):        Apply rateModifier to biological parameters based on dose
          ↓
Priority 4 (stepQAction): Perceive current state → Decide action → Administer dose
          ↓
Priority 5 (updateModel): Perceive new state → Calculate reward → Update Q-table
          ↓
Check terminal state:     If stationaryStep==50 or tick==1200 → Episode finished
```

### 1.3 Success Metrics
- **Minimization of neuronal death** (target: ≤1 degenerated neuron)
- **Maximization of average neuronal health** (target: ~28/30 health units)
- **Dose efficiency**: reach target health with minimal dose (toxicity trade-off)
- **Robustness**: function across varying difficulty levels (curriculum learning)

---

## 2. Environment Policy Concept

### 2.1 What is the Policy
The **Policy** is a Singleton manager that centralizes and modifies **5 key biological parameters** of the Substantia Nigra environment:

| Parameter | Biological Meaning | Base Value | Effect |
|-----------|-------------------|-----------|--------|
| **CYTO_RELEASE_RATE** | Rate of cytokine release from activated microglia | 0.5 | ↓ with dose (1/modifier) |
| **DEGENERATION_RATE** | Rate of stress-induced neuronal death | 1.0 | ↓ with dose (1/modifier) |
| **CYTO_NEURON_THRESHOLD** | Cytokine threshold that stresses a neuron | 5.0 | ↑ with dose (3.0×modifier) |
| **CYTO_MICROGLIA_THRESHOLD** | Cytokine threshold that activates microglia | 3.0 | ↑ with dose (3.0×modifier) |
| **EVAPORATION_RATE** | Dissipation of cytokine field | 0.05 | Variable (1.0 - modifier/50) |

### 2.2 Policy Architecture
```java
Policy policy = Policy.getInstance();  // Singleton

// Access to modifiable parameters
policy.getParam(StatType.CYTO_RELEASE_RATE).setModifier(1 / rateModifier);
policy.getParam(StatType.DEGENERATION_RATE).setModifier(1 / rateModifier);
policy.getParam(StatType.CYTO_NEURON_THRESHOLD).setModifier(rateModifier * 3.0);
policy.getParam(StatType.CYTO_MICROGLIA_THRESHOLD).setModifier(rateModifier * 3.0);
policy.getParam(StatType.EVAPORATION_RATE).setModifier(1.0 - rateModifier / 50);

// Effective value = baseValue × modifier
double effectiveValue = policy.getParam(StatType.CYTO_RELEASE_RATE).getEffectiveValue();
```

### 2.3 Biological Principle of Dosing
The drug (GLP-1) **reduces inflammation** through:
1. **Inhibition of cytokine release** (↓ CYTO_RELEASE_RATE)
2. **Neuronal protection** (↓ DEGENERATION_RATE, ↑ threshold values)
3. **Accelerated clearance** (↑ EVAPORATION_RATE with moderation)

The modifier is a **monotonically increasing function of dose**: dose↑ → modifier↑ → protection↑

### 2.4 Limitations and Design Constraints
- **Does not directly modify** already-initiated neuronal death dynamics
- **Toxicity** is penalized in the reward function (dose cost)
- **Biological delay** is not modeled (instantaneous effect, simplifying assumption)
- **Saturation**: at very high doses (>2.0), modifier does not guarantee 100% survival

---

## 3. State Space: Substantia Nigra State

### 3.1 State Definition
The state represents the **agent's perception** of the inflammation level in the neuronal compartment.
It is a 3D discretization of the continuous biological state.

### 3.2 State Dimensions
```
SubstanciaNigraState(degeneratedNeuronCount, stressedNeuronCount, inflammatedMicrogliaCount, 
                     actualDegenNeuron, averageNeuronHealth, currentGLP1dose)
```

| Dimension | Range | Granularity | Meaning |
|-----------|-------|-------------|---------|
| **Dead neurons (DEG)** | [0, 31] | Discrete | Dead neurons (irreversible state) |
| **Stressed neurons (STRESS)** | [0, 31] | Discrete | Low-health neurons (<30% max) |
| **Microglia inflammation (INFLAM)** | [0, 10] | Discrete (aggregated bucket) | Proxy for systemic inflammation |
| **Actual degeneration** | [0, 31] | Continuous | Aggregated total health (logging) |
| **Average neuron health** | [0, 30] | Continuous | Average health for analysis |
| **Current dose** | [0, 2.0] | Continuous | Administered dose (logging, not in hashCode) |

### 3.3 State Space Discretization
```
Q-learning state space: DEG × STRESS × INFLAM
                      = 32 × 32 × 11 ≈ 11,264 possible states
                      
Effective space: ~7,700 states
(many physically impossible states, e.g., DEG + STRESS > 31)
```

**Critical note**: The dose `currentGLP1dose` is **intentionally excluded from hashCode**:
- Dose is an **action**, not a **state**
- State must be invariant to control choices
- This avoids combinatorial explosion of state space

### 3.4 State Encoding for Q-Learning
```java
// hashCode maintains uniqueness property for biological state
// independent of dose
@Override
public int hashCode() {
    return degeneratedNeuronCount + 31 * stressedNeuronCount;
    // inflammatedMicrogliaCount already included in discrete calculation
}
```

### 3.5 Boundary Conditions and Terminal States
```
Initial state (curriculum):
  - Easy (run 0-50):    1 dead, 0 stressed
  - Medium (run 51-100): 3 dead, 2 stressed
  - Hard (run 101+):     5 dead, 5 stressed

Terminal conditions:
  1. Victory:    stationaryStep == 50 (inflammation stays low for 50 ticks)
  2. Timeout:     tick == 1200 (episode canceled, opportunity cost)
  
Sink state: Dead neurons == 31 (neuronal population extinct)
```

### 3.6 State Evolution
State evolves according to:
- **Direct perception**: Treatment agent counts neurons in Context
- **Frequency**: Every tick (1 biological unit)
- **Source**: Neuron population queries in ParkinsonBuilder context

---

## 4. Action Space: Discrete Dosages

### 4.1 Action Definition
An action is a **dose of GLP-1 drug** administered in the current tick.

### 4.2 Discretized Action Space
```
Actions = {0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0,
           1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 2.0}

|A| = 21 discrete actions
MAX_DOSE = 2.0

Granularity: 0.1 dose units
```

### 4.3 Action Representation
```java
public class Dosage implements Action {
    private double dose;  // [0.0, 2.0]
    
    public Dosage(double dose) {
        this.dose = Math.max(0.0, Math.min(2.0, dose));  // Clamp
    }
}
```

### 4.4 Action Selection: Alternative Policies
The system supports **4 PolicyMode** for baseline comparison and ablation studies:

| PolicyMode | Formula | Meaning | Use Case |
|-----------|---------|---------|----------|
| **RL** | ε-greedy Q-learning | Learned policy | Primary |
| **NO_DOSE** | dose = 0.0 | No treatment | Control baseline |
| **MAX_DOSE** | dose = 2.0 | Maximum administration | Upper biological bound |
| **PROPORTIONAL** | dose = (deg + 0.5×stress)/tot × 2.0 | Fixed heuristic | Intelligent baseline |

```java
public enum PolicyMode {
    RL,           // Q-learning epsilon-greedy
    NO_DOSE,      // Always 0.0
    MAX_DOSE,     // Always 2.0
    PROPORTIONAL  // Heuristic: dose ~ disease severity
}
```

### 4.5 Q-Learning Exploration-Exploitation
For the **RL** policy, action selection follows **epsilon-greedy**:

```
ε-decay (dual scale):
  ε = max(0.05, 0.8 × 0.996^(batch_run) × 0.999^(tick))

Action:
  With probability ε:      Select random action (exploration)
  With probability (1-ε):  Select argmax_a Q(state, action) (exploitation)
```

**Interpretation**:
- In early batch runs: ε high (more exploration)
- In late episodes of batch: ε low (policy exploitation)
- Minimum ε = 0.05 (always explore 5% of time)

### 4.6 Dose Administration
Once the action (dose) is chosen, it is:
1. **Stored** as `currentGLP1dose`
2. **Transformed** to `rateModifier` (see section 5)
3. **Applied** to 5 biological parameters (see section 5)
4. **Logged** in output CSV for post-hoc analysis

---

## 5. Mapping: Action-Dosage → Parameter Modification

### 5.1 Transformation Pipeline
```
Action (Dosage) → rateModifier → ModifiableParameter modifiers → Biological Parameters
```

### 5.2 Transformation Function: Dose → rateModifier
The dosage is **transformed into a biological modifier** via a piecewise function:

```java
double rateModifier;
if (dose < 1.6) {
    // Conservative regime (logarithmic)
    rateModifier = 1 + ln(1 + dose);
    // Examples:
    //   dose=0.0 → modifier=1.00 (no effect)
    //   dose=0.5 → modifier=1.40
    //   dose=1.0 → modifier=1.69
    //   dose=1.5 → modifier=1.84
} else {
    // Aggressive regime (exponential)
    double baseValue = 1 + ln(1 + 1.6) ≈ 1.86;
    double excessDose = dose - 1.6;
    rateModifier = 1.86 + 2.5 × excessDose;
    // Examples:
    //   dose=1.6 → modifier=1.86
    //   dose=1.8 → modifier=2.36
    //   dose=2.0 → modifier=2.86
}
```

**Biological rationale**:
- **Low doses** (< 1.6): sublinear effect (initial saturation curve)
- **High doses** (≥ 1.6): accelerated linear effect (additional boost for emergencies)

### 5.3 Application to Biological Parameters

Once `rateModifier` is calculated, it is applied to 5 parameters:

#### 5.3.1 CYTO_RELEASE_RATE (cytokine release inhibition)
```
Base value:         0.5 cytokine/tick
Modification:       modifier = 1 / rateModifier
Biological effect:  Dose ↑ → cytokine release ↓

Examples:
  dose=0.0  → 1/1.00=1.00 → CYTO_RELEASE_RATE = 0.5 (no change)
  dose=1.0  → 1/1.69=0.59 → CYTO_RELEASE_RATE = 0.30 (↓ 40%)
  dose=2.0  → 1/2.86=0.35 → CYTO_RELEASE_RATE = 0.18 (↓ 65%)
```

#### 5.3.2 DEGENERATION_RATE (neuronal protection)
```
Base value:         1.0 health_loss/tick
Modification:       modifier = 1 / rateModifier
Biological effect:  Dose ↑ → neuronal death ↓

Examples:
  dose=1.0  → DEGENERATION_RATE = 0.59 (↓ 41%)
  dose=2.0  → DEGENERATION_RATE = 0.35 (↓ 65%)
```

#### 5.3.3 CYTO_NEURON_THRESHOLD (stress protection)
```
Base value:         5.0 cytokine
Modification:       modifier = rateModifier × 3.0  [AMPLIFICATION ×3.0]
Biological effect:  Dose ↑ → neurons remain healthy longer

Examples:
  dose=0.0  → 1.00×3.0=3.00   → threshold=3.00 (base=5.0, DOWN 40%)
  dose=1.0  → 1.69×3.0=5.07   → threshold=5.07 (base=5.0, ~no change)
  dose=2.0  → 2.86×3.0=8.58   → threshold=8.58 (base=5.0, UP 71%)

Interpretation: With GLP-1, neurons tolerate more cytokine before stressing
```

#### 5.3.4 CYTO_MICROGLIA_THRESHOLD (brake on microglia activation)
```
Base value:         3.0 cytokine
Modification:       modifier = rateModifier × 3.0
Biological effect:  Dose ↑ → microglia less reactive

Examples:
  dose=1.0  → threshold=5.07 (base=3.0, UP 69%)
  dose=2.0  → threshold=8.58 (base=3.0, UP 186%)
```

#### 5.3.5 EVAPORATION_RATE (cytokine field clearance)
```
Base value:         0.05 (5% clearance/tick)
Modification:       modifier = 1.0 - rateModifier / 50
Biological effect:  Moderate (not strongly dose-dependent)

Examples:
  dose=0.0  → 1.0 - 1.00/50 = 0.980 → EVAPORATION = 0.980×0.05 ≈ 0.049
  dose=1.0  → 1.0 - 1.69/50 = 0.966 → EVAPORATION ≈ 0.048
  dose=2.0  → 1.0 - 2.86/50 = 0.943 → EVAPORATION ≈ 0.047
```

### 5.4 Complete Flow Diagram

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
  (inhibition)        (protection)      THRESHOLD
                                         CYTO_MICROGLIA_
                                         THRESHOLD
                                         (protection)
```

### 5.5 Integrated Biological Effects
```
Scenario: Microglia cascade is inflamed, neurons stressed

RL Action: dose = 1.2
  → rateModifier = 1 + ln(1 + 1.2) ≈ 1.74

Effects:
  ✓ CYTO_RELEASE_RATE ÷ 1.74  → fewer cytokines produced
  ✓ DEGENERATION_RATE ÷ 1.74  → less neuronal death
  ✓ CYTO_NEURON_THRESHOLD ×5.22  → neurons tolerate more stress
  ✓ CYTO_MICROGLIA_THRESHOLD ×5.22  → microglia less reactive

Integrated result:
  1. Already-produced cytokines evaporate more slowly (↓ EVAPORATION)
  2. But new cytokines are produced less (↓ RELEASE_RATE)
  3. Neurons are more resistant (↑ thresholds)
  4. Microglia cascade shuts down faster

→ Initial inflammation is contained, but NOT always stopped
```

### 5.6 Mapping Limitations
1. **High dose does not guarantee victory**: with already-initiated microglia cascade, even dose=2.0 may lose
2. **No biological delay**: modifiers applied instantaneously (simplification)
3. **Linearity in integration**: effects do not interact non-linearly
4. **No saturation**: model not calibrated beyond dose=2.0

---

## 6. Reinforcement Learning Cycle (BONUS - if desired)

### 6.1 Q-Learning Update Rule
```
Q(s,a) ← Q(s,a) + α × [R(s,a,s') + γ × max_a' Q(s', a') - Q(s,a)]

Parameters:
  α (learning rate)     = 0.1
  γ (discount factor)   = 0.9
  R (reward function)   = weighted sum of biological components
```

### 6.2 Reward Function Shaping
See Section 7 for detailed mathematical formulation.

### 6.3 Terminal States and Episode
```
Episode terminates IF:
  1. stationaryStep == 50  (victory: inflammation stable for 50 ticks)
  2. tick == 1200          (timeout: give up)
  3. DEG == 31             (defeat: population extinct)

At termination:
  → Q-table serialized to JSON
  → Episode metrics written to CSV
  → New episode starts with new stochasticity
```

---

## 7. Reward Function: Mathematical Formulation

The reward function is a **linearly weighted combination** of biological and control objectives:

$$R(s, a, s') = R_{\text{dose}} + R_{\text{death1}} + R_{\text{death2}} + R_{\text{trend}} + R_{\text{bonus\_dose}} + R_{\text{bonus\_improve}}$$

### Component Definitions

**1. Dose Cost** (penalizes toxicity)
$$R_{\text{dose}} = -0.3 \times \frac{\text{dose}}{2.0}$$

- Magnitude: -0.15 (at max dose)
- Rationale: Discourage unnecessary high doses; favor dose-efficient policies

**2. Immediate Death Penalty** (penalizes health loss)
$$R_{\text{death1}} = -0.3 \times \frac{\text{actualDegeneration}}{31}$$

- Magnitude: -0.3 (when all neurons dying)
- Rationale: Penalize ongoing neuronal degradation

**3. Dead Count Penalty** (penalizes neuron death)
$$R_{\text{death2}} = -0.4 \times \frac{\text{deadNeurons}}{31}$$

- Magnitude: -0.4 (when all neurons dead)
- Rationale: Strong penalty for irreversible population loss

**4. Negative Trend Penalty** (penalizes worsening)
$$R_{\text{trend}} = \begin{cases} 
-2.0 \times \frac{\max(0, \Delta \text{deadNeurons})}{31} & \text{if } \Delta \text{deadNeurons} > 0 \\
0 & \text{otherwise}
\end{cases}$$

- Magnitude: -2.0 (strong penalty when deaths increase)
- Rationale: Aggressively discourage policies that allow cascades

**5. Dose Bonus** (rewards effective treatment)
$$R_{\text{bonus\_dose}} = \begin{cases} 
0.8 \times \frac{\text{inflammation}}{31} \times \frac{\text{dose}}{2.0} & \text{if } \Delta \text{deadNeurons} \leq 0 \\
-0.1 & \text{if } \Delta \text{deadNeurons} > 0
\end{cases}$$

- Magnitude: 0.8 (when high inflammation + high dose + no worsening)
- Rationale: Reward dose calibration; penalize ineffective dosing when things worsen

**6. Improvement Bonus** (rewards stability)
$$R_{\text{bonus\_improve}} = \begin{cases}
0.5 & \text{if } \Delta \text{deadNeurons} = 0 \text{ AND } \text{dose} > 0.2 \\
0 & \text{otherwise}
\end{cases}$$

- Magnitude: 0.5 (when stabilization achieved with non-trivial dose)
- Rationale: Encourage active stabilization over passive survival

### Reward Range
$$R_{\text{min}} \approx -1.4 \quad \text{(all penalties active)}$$
$$R_{\text{max}} \approx +0.8 \quad \text{(all bonuses active)}$$

**Interpretation**: 
- Rewards are normalized to avoid scale issues
- Multi-component design encourages trade-off learning
- Negative trend penalty is strongest (avoids catastrophe)

---

## 8. Future Updates

### 8.1 Technical Sections (For In-Depth Review)
- [ ] **Java Implementation**: Class diagram, key methods, thread synchronization
- [ ] **Persistence**: How Q-table is serialized/deserialized, JSON format
- [ ] **Data Logging**: CSV schema, traced metrics, sampling frequency
- [ ] **Curriculum Learning**: batch_run → difficulty dynamics

### 8.2 Scientific Sections (For Biological Contribution)
- [ ] **Biological Validation**: Comparison of rateModifier with empirical GLP-1 data
- [ ] **Parameterization**: Source of 5 base parameters (literature / fitting)
- [ ] **Sensitivity Analysis**: Effect of varying reward function coefficients
- [ ] **Generalizability**: How training transfers across different initializations

### 8.3 Experimental Sections (For Reproducibility)
- [ ] **Baseline Comparison**: Results RL vs NO_DOSE vs PROPORTIONAL vs MAX_DOSE
- [ ] **Ablation Studies**: Effect of varying individual reward parameters
- [ ] **Convergence Plots**: Q-table convergence rate, epsilon decay, episode reward trend
- [ ] **Policy Heatmap**: Visualization of learned policy (state → argmax dose)

### 8.4 Future Improvements
- [ ] **Transfer Learning**: Policy pre-trained on easy → transfer to hard
- [ ] **Continuous Actions**: Extension from 21 discrete dosages to continuous actions
- [ ] **Multi-agent**: Collaborative treatment across multiple brain regions
- [ ] **Imitation Learning**: Warm-start Q-learning with human expert

---

## Internal References
- **Treatment.java**: `src/parkinson/agent/passive/Treatment.java`
- **Policy.java**: `src/parkinson/Policy.java`
- **SubstanciaNigraState.java**: `src/parkinson/learning/SubstanciaNigraState.java`
- **TreatmentModel.java**: `src/parkinson/learning/TreatmentModel.java`
- **ParkinsonBuilder.java**: `src/parkinson/ParkinsonBuilder.java`
- **Baseline Results**: `outputKami/outputKamirlConvergence_{RL,NODOSE,MAXDOSE,PROPORTIONAL}.csv`
