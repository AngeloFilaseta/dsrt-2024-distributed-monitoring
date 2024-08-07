import pandas as pd
import seaborn as sns
import os
import matplotlib.pyplot as plt
from matplotlib.ticker import ScalarFormatter
from matplotlib.pyplot import figure
import sys
figure(figsize=(10, 6))

# Seaborn
sns.set_theme()

# Load the CSV file
directory = ' '.join(sys.argv[1:])

dataframes = []

for filename in os.listdir(directory):

    col_names = ['Time', 'Nodes', 'Limited', 'Full']
    f = os.path.join(directory, filename)
    df = pd.read_csv(f, sep=" ", names=col_names, comment='#')
    df["Nodes"] = df["Nodes"].astype(int)
    melted_df = pd.melt(
        df,
        id_vars=['Time', 'Nodes'],
        value_vars=['Limited', 'Full'],
        var_name='Type',
        value_name='Size'
    )
    melted_df = melted_df[melted_df["Time"] < 400]
    melted_df["Time"] = melted_df["Time"].round(0)
    melted_df['Size (kB)'] = melted_df['Size'] / 1000
    dataframes.append(melted_df)

data = pd.concat(dataframes, ignore_index=True)

plt.rc('text.latex', preamble=r'\usepackage{amsmath,amssymb,amsfonts,amssymb,graphicx}')
params = {"ytick.color" : "black",
          "xtick.color" : "black",
          "axes.labelcolor" : "black",
          "text.usetex" : True,
          "font.family" : "serif",
          "font.size" : 18,
          "font.serif" : ["Computer Modern Serif"]}
plt.rcParams.update(params)

# Assign plots
limited_df = data.query('Type == "Limited"')
full_df = data.query('Type == "Full"')
full_size_plot = sns.lineplot(data=full_df, x="Time", y="Size (kB)", color="#B8DE29")
limited_size_plot = sns.lineplot(data=limited_df, x="Time", y="Size (kB)", color="#482677")
ax2 = full_size_plot.twinx()
nodes_plot = sns.lineplot(data=limited_df, x="Time", y="Nodes", color="#000000", linewidth=1, ax=ax2)


# ✨ Aesthetic ✨
full_size_plot.set_xlabel('Simulation time', fontsize=20)
full_size_plot.set_ylabel('Size (kB)', fontsize=20)
full_size_plot.set_yscale("log")
ax2.set_yscale("log")

full_size_plot.yaxis.set_major_formatter(ScalarFormatter())
full_size_plot.minorticks_off()
ax2.yaxis.set_major_formatter(ScalarFormatter())
ax2.minorticks_off()


ax2.set_yticks([1, 2, 5, 10, 15, 20, 30])
full_size_plot.set_yticks([0.1, 0.5, 1, 5, 10, 25])
ax2.yaxis.set_tick_params(labelsize=18)
full_size_plot.yaxis.set_tick_params(labelsize=18)
full_size_plot.xaxis.set_tick_params(labelsize=18)

nodes_plot.set_ylabel('Number of nodes', fontsize=20)
nodes_plot.set_ylim(0, data["Nodes"].max() + 10)
# LEGEND
custom_lines = [
    plt.Line2D([0], [0], color="#000000", lw=3),
    plt.Line2D([0], [0], color="#482677", lw=3),
    plt.Line2D([0], [0], color="#B8DE29", lw=3)
]
ax2.legend(custom_lines, [
    "Number of nodes",
    r'\textit{Specific Query} response size',
    r'\textit{Baseline Query} response size'
], loc="lower right", fontsize=18)
plt.title('Influence of Node Quantity on Response Size ', fontsize=24)

plt.rcParams.update({"text.usetex": True})
plt.savefig("scalability.pdf")
plt.show()
plt.clf()
