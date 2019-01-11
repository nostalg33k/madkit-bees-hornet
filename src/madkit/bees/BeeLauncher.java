/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit_Demos.
 * 
 * MaDKit_Demos is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit_Demos is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit_Demos. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.bees;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Message;
import madkit.message.EnumMessage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.lang.Thread.sleep;

/**
 * The agent that launches the simulation
 *
 * @version 2.0.0.3
 * @author Fabien Michel
 */
public class BeeLauncher extends madkit.kernel.Agent {

	public static final String SIMU_GROUP = "bees";
	public static final String BEE_ROLE = "bee";
	public static final String QUEEN_ROLE = "queen";
	public static final String FOLLOWER_ROLE = "follower";
	public static final String PREY = "prey";
	public static final String VIEWER_ROLE = "viewer";
	public static final String SCHEDULER_ROLE = "scheduler";
	public static final String LAUNCHER_ROLE = "launcher";
	public static final String COMMUNITY = "buzz";

	private static final int INITIAL_BEES_NB = 10;
	private ArrayList<AbstractAgent> queensList = new ArrayList<>();
	private ArrayList<AbstractAgent> beesList = new ArrayList<>(INITIAL_BEES_NB * 2);
	private ArrayList<AbstractAgent> hornetsList = new ArrayList<>();
	private boolean randomMode = true;

	@Override
	protected void activate() {
		getLogger().info(() -> "Launching bees simulation...");
		createGroup(COMMUNITY, SIMU_GROUP, false, null);
		requestRole(COMMUNITY, SIMU_GROUP, LAUNCHER_ROLE, null);

		long startTime = System.nanoTime();
		launchHornet(1);

		BeeScheduler beeScheduler = new BeeScheduler();
		launchAgent(beeScheduler, false);
		madkit.bees.BeeViewer beeViewer = new BeeViewer(beeScheduler);
		launchAgent(beeViewer, true);

		pause(3000);


		launchBees(INITIAL_BEES_NB);

		getLogger().info(() -> "launch time : " + (System.nanoTime() - startTime));

		launchQueens(1);




	}

	/**
	 * So that I can react to {@link BeeLauncherAction#RANDOM_MODE} message
	 */
	@SuppressWarnings("unused")
	private void randomMode(boolean on) {
		randomMode = on;
	}

	enum BeeLauncherAction {

		RANDOM_MODE,
		LAUNCH_BEES,
	}

	@Override
	protected void live() {
		while (isAlive()) {
			Message m = waitNextMessage(500 + (int) (Math.random() * 2000));
			if (m != null) {
				proceedEnumMessage((EnumMessage<?>) m);

			}
			/*if (randomMode) {
				killBees(false, 150);
				if (Math.random() < .8) {
					if (Math.random() < .5) {
						if (queensList.size() > 1)
							if (queensList.size() > 7)
								killBees(true, (int) (Math.random() * 7) + 1);
							else
								killBees(true, (int) (Math.random() * 2) + 1);
					}
					else if (queensList.size() < 10)
						launchQueens((int) (Math.random() * 2) + 1);
				}
				else if (Math.random() < .3) {
					if (beesList.size() < 200000 && Runtime.getRuntime().freeMemory() > 100000) {
						launchBees((int) (Math.random() * 15000) + 5000);
					}
				}
				else {
					killBees(false, (int) (Math.random() * 500) + 1);
				}
			}*/
		}
	}

	@Override
	protected void end() {
		queensList = null;
		beesList = null;
		hornetsList = null;
		getLogger().info("I am done. Bye !");
	}

	private void launchBees(int numberOfBees) {
		getLogger().info(() -> "Launching " + numberOfBees + " bees");
		//greatly optimizes the launching time
		final List<AbstractAgent> beesBucket = launchAgentBucket(
				Bee.class.getName(),
				numberOfBees,
				COMMUNITY + "," + SIMU_GROUP + "," + BEE_ROLE,
				COMMUNITY + "," + SIMU_GROUP + "," + FOLLOWER_ROLE,
				COMMUNITY + "," + SIMU_GROUP + "," + PREY);
		beesList.addAll(beesBucket);
	}

	private void launchQueens(int numberOfQueens) {
		getLogger().info(() -> "Launching " + numberOfQueens + " queen bees");
		for (int i = 0; i < numberOfQueens; i++) {
			final madkit.bees.QueenBee newQueen = new QueenBee();
			launchAgent(newQueen);
			queensList.add(newQueen);
		}
	}

	private void launchHornet(int numberOfHornets) {
		getLogger().info(() -> "Launching " + numberOfHornets + " hornets");
		for (int i = 0; i < numberOfHornets; i++) {
			final Hornet newHornet = new Hornet();
			launchAgent(newHornet);
			hornetsList.add(newHornet);
		}

	}

	private void killBees(boolean queen, int number) {
		List<AbstractAgent> l;
		int j = 0;
		if (queen)
			l = queensList;
		else
			l = beesList;
		for (final Iterator<AbstractAgent> i = l.iterator(); i.hasNext() && j < number; j++) {
			if (j % 100 == 0) {
				Thread.yield();
			}
			final AbstractAgent a = i.next();
			if (a != null) {
				i.remove();
				killAgent(a);
			}
			else
				break;
		}
	}

	public static void main(String[] args) {
		executeThisAgent(1, false, args);
	}

}


