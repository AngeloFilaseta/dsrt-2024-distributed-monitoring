# dsrt-2024-distributed-monitoring

## Requirements
This project requires a Java 17 or higher version.
If you have no Java installed,
it is recommended to install the latest LTS Java from AdoptOpenJDK (unless a new LTS is out and Gradle does not yet support it).

## Experiments
Before proceeding with the experiments: 
- local ports from `1313` to `1412` should be available, along with the port `9090`;
- install the version of Python indicated in `.python-version` (or use [pyenv](https://github.com/pyenv/pyenv));
- Upgrade pip to the latest version: `pip install --upgrade pip`;
- Install the requirements: `pip install -r requirements.txt`.

First of all clone the repository and navigate to the project directory:

```shell
git clone https://github.com/AngeloFilaseta/dsrt-2024-distributed-monitoring.git
cd dsrt-2024-distributed-monitoring
```

Some Gradle tasks will be available for each experiment.

The experiments were originally executed on the following machine:
- CPU: Intel(R) Core(TM) i7-8700T CPU @ 2.40GHz
- RAM: 16GiB System memory
- OS: Manjaro Linux

### Viability

To execute the experiment highlighting the *Viability* goal, run the following command:

```shell
./gradlew runOneRootMonitorExperiment # UNIX & Bash Emulators
gradlew.bat runOneRootMonitorExperiment # Windows
```

The default browser should open a new tab at [http://localhost:9090/](http://localhost:9090/).
If the browser does not open automatically, it is possible to manually open the page by visiting the URL.
The Alchemist Monitor GUI should be visible now.

1) use the *Selection Form* to choose how the monitor configuration.
2) add the addresses of the clients to monitor using the *Navbar*. 
  Three simulation are available to connect, at addresses:`http://localhost:1313/`, `http://localhost:1314/`, `http://localhost:1315/`,
  The `Add All Available Clients` client button will automatically add all the clients in one shot.

Press the `Play` button to start the simulations.
Four plot will appear on the screen.
Three of them, characterized by a green line,
display the evolution of the local success metric for each client.
The last one, characterized by a purple line,
displays the same metric,
aggregated over all clients.

### Scalability
To execute the experiment highlighting the *Scalability* goal, run the following command:

```shell
./gradlew runOneRootResponseSizeExperiment # UNIX & Bash Emulators
gradlew.bat runOneRootResponseSizeExperiment # Windows
```

The required time for completion was estimated to be around 1 hour and 30 minutes on the machine described in [Experiments](#experiments).

Raw metrics will be available in the `evaluation/oneRootResponseSize` directory.
It is possible to generate the plot by running the following command:
```shell
python evaluation/scalability.py evaluation/oneRootResponseSize
```

If you want to re-generate the plots using the available data, run the following command instead:

```shell
python evaluation/scalability.py evaluation/oneRootResponseSize_presaved
```

### Efficiency

To execute the experiment highlighting the *Scalability* goal, run the following command:

```shell
./gradlew runOneRootLostUpdateUselessPollingExperiment # UNIX & Bash Emulators
gradlew.bat runOneRootLostUpdateUselessPollingExperiment # Windows
```

**WARNING**: This experiments takes a long time to complete.

The required time for completion was estimated to be around 1 hour and 30 minutes on the machine described in [Experiments](#experiments).

Raw metrics will be available in the `evaluation/oneRootLostUpdateUselessPolling` directory.
It is possible to generate the plot by running the following command:
```shell
python evaluation/efficiency.py evaluation/oneRootLostUpdateUselessPolling
```

If you want to re-generate the plots using the available data, run the following command instead:

```shell
python evaluation/efficiency.py evaluation/oneRootLostUpdateUselessPolling_presaved
```
