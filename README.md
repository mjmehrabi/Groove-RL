# Deep Reinforcement Learning In Groove Graph Transformation
## Model Checking Using Deep Reinforcement Learning Implemented In The Groove Graph Transformation Tool Set

1. This implementation is based on [Groove 5.7.2 source code](https://groove.ewi.utwente.nl) and uses **Deep Q-Network** and **Double Deep Q-Network** to verify reachability property in complex software system specified by graph transformation.

2. To declare a dedicated reward function for your model, define it in the `ClosingStrategy` class that placed in the `groove\explore\strategy` package, and replace your function call with line 181.
Afterward, in the Model Checking dialog, select **Reward 2 (Dedicated)** option to use your defined reward function.

3. You can also change neural network configuration in the `RLAgent` abstract class in the `groove\verify` package.

4. You can define your desired experience reply memory in the `groove\verify` package and implement the`IExperienceReplayMemory` interface.
Afterward, put it in your agent class like `DDQNAgent` or `DQNAgent` to implement it.

5. Created And Modified Classes

| Package Name        | Class Name           | Description  | Type
| :-------------: |:-------------:| :----| :----:
| groove.gui.action      | RLDialogAction | Create a new dialog for the new approach | `Created`
| groove.gui.action      | RLExploreAction | Exploring for new approach | `Created`
| groove.gui.dialog      | RLDialog | Create a dialog box and its graphical user interface | `Created` `Modified`
| groove.verify      | RL | Implement the main section of the algorithm, manage each Episode and check the outputs of the main section of the algorithm | `Created`
| groove.verify      | RLExploringItem | The required data during the execution of the algorithm | `Created`
| groove.verify      | RLAgent | Abstract class as parent class for different classes to create different DQN algorithms | `Created`
| groove.verify      | IExperienceReplayMemory | Interface for different classes to create different Replay Memory | `Created`
| groove.verify      | SimpleExperienceReplayMemory | Simple Experience Replay Memory | `Created`
| groove.verify      | Memory | Data model to store experiences | `Created`
| groove.verify      | DDQNAgent | Double Deep Q-Network Agent | `Created`
| groove.gui      | Options | The name the dialog in the verify menu | `Modified`
| groove.gui      | Simulator | Add a new option menu for new approach | `Modified`
| groove.gui.action      | ActionStore | Use the created menus | `Modified`
| groove.explore      | Exploration | A function to manage the overall process of each exploration | `Modified`
| groove.explore.strategy      | ClosingStrategy | The main body of the algorithm presented in the RLdoNext method, as well as dedicated rewards and feature selection | `Modified`
| groove.explore.strategy      | Strategy | How to explore the states according to the nature of the strategy | `Modified`
| groove.explore.strategy      | Strategy | RLdoNext method structure | `Modified`
| groove.explore.strategy      | LTLStrategy | RLdoNext method structure | `Modified`
| groove.explore.strategy      | LinearStrategy | RLdoNext method structure | `Modified`
| groove.explore.strategy      | SymbolicStrategy | RLdoNext method structure | `Modified`
| groove.explore.strategy      | RateStrategy | RLdoNext method structure | `Modified`
| groove.explore.strategy      | BFSStrategy | RLdoNext method structure | `Modified`
| groove.explore.strategy      | DFSStrategy | RLdoNext method structure | `Modified`
| groove.explore.strategy      | ExploreStateStrategy | RLdoNext method structure | `Modified`

## Thanks
* [Arend Rensink, Project Leader Of Groove Graph Transformation Tool Set][A. Rensink]
* [Vahid Rafe, Associate Professor Of Arak University, Faculty Of Computer Engineering][V. Rafe]
* [DeepLearning4J Team For Their Unique Deep Learning Java Library][DL4J]

[V. Rafe]: https://scholar.google.com/citations?user=JdL7r00AAAAJ&hl=en
[A. Rensink]: https://github.com/rensink
[DL4J]: https://github.com/eclipse/deeplearning4j
