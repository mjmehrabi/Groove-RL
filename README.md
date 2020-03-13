# Model Checking Using Deep Reinforcement Learning Implemented In The Groove Graph Transformation Tool Set

1. This implementation is based on [Groove 5.7.2 source code](https://groove.ewi.utwente.nl) and uses **Deep Q-Network** and **Double Deep Q-Network** to verify reachability property in complex software system specified by graph transformation.

2. To declare a dedicated reward function for your model, define it in the `ClosingStrategy` class that placed in the `groove\explore\strategy` package, and replace your function call with line 181.
Afterward, in the Model Checking dialog, select **Reward 2 (Dedicated)** option to use your defined reward function.

3. You can also change neural network configuration in the `RLAgent` abstract class in the `groove\verify` package.

4. You can define your desired experience reply memory in the `groove\verify` package and implement the`IExperienceReplayMemory` interface.
Afterward, put it in your agent class like `DDQNAgent` or `DQNAgent` to implement it.

## Thanks
* [Arend Rensink, Project Leader of Groove Graph Transformation Tool Set][A. Rensink]
* [Vahid Rafe, Associate Professor of Arak University, Faculty of Computer Engineering][V. Rafe]

[V. Rafe]: https://scholar.google.com/citations?user=JdL7r00AAAAJ&hl=en
[A. Rensink]: https://github.com/rensink