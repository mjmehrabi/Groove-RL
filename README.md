# Groove-RL
## Model Checking Using Deep Reinforcement Learning Implemented In The Groove Graph Transformation Tool Set

1. This implementation is based on [Groove 5.7.2 source code](https://groove.ewi.utwente.nl) and uses **Deep Q-Network** and **Double Deep Q-Network** to verify reachability property in complex software system specified by graph transformation.
2. To declare dedicated reward function for your model, define it in the `ClosingStrategy` class that placed in `groove\explore\strategy` package, and replace your function call with line 181.
Afterwards, in the Model Checking dialog, select **Reward 2 (Dedicated)** option to use you defined reward function.
3. You can also change neural network configuration in `RLAgent` abstract class in the `groove\verify` package.
4. You can define your desired experience reply memory in the `groove\verify` package and implement `IExperienceReplayMemory` interface.
afterwards put it in your agent class like `DDQNAgent` or `DQNAgent` to implement it.

###Thanks
* [Arend Rensink, Project Lead Of Groove Graph Transformation Tool Set](https://github.com/rensink)
* [Vahid Rafe, Associate Professor of Arak University, Faculty Of Computer Engineering](https://scholar.google.com/citations?user=JdL7r00AAAAJ&hl=en)
