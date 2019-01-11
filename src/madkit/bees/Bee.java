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

import java.awt.Point;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import madkit.bees.AbstractBee;
import madkit.bees.BeeInformation;
import madkit.kernel.AgentAddress;
import madkit.kernel.Message;
import madkit.message.ObjectMessage;
import madkit.message.StringMessage;
import org.omg.CORBA.Request;


import static java.lang.Thread.sleep;
import static madkit.bees.BeeLauncher.*;

/**
 * @version 2.3
 * @author Fabien Michel, Olivier Gutknecht
 */
public class Bee extends AbstractBee {

    /**
     * 
     */
    private static final long serialVersionUID = -2393301912353816186L;
    BeeInformation leaderInfo = null;
    AgentAddress leader = null;
	static int border = 20;
	boolean kill = false;

    @Override
    public void activate() {
	requestRole(COMMUNITY, SIMU_GROUP, BEE_ROLE, null);
	requestRole(COMMUNITY, SIMU_GROUP, FOLLOWER_ROLE, null);
	requestRole(COMMUNITY, SIMU_GROUP, PREY, null);
	broadcastMessageWithRole(COMMUNITY, SIMU_GROUP, "hornet", new ObjectMessage<>(myInformation),"prey");
	broadcastMessage(COMMUNITY, SIMU_GROUP, FOLLOWER_ROLE, new ObjectMessage<>(myInformation));

    }

    /** The "do it" method called by the activator */
    @Override
    public void buzz() {
    	updateLeader();
		super.buzz();

		if (beeWorld != null) {
			// check to see if the bee hits the edge
			final Point location = myInformation.getCurrentPosition();
			if (location.x < border || location.x > (beeWorld.getWidth() - border)) {
				dX = -dX;
				location.x += (dX);
			}
			if (location.y < border || location.y > (beeWorld.getHeight() - border)) {
				dY = -dY;
				location.y += (dY);
			}
		}
    }

    /**
     * 
     */
    private void updateLeader() {
	ObjectMessage<BeeInformation> m = (ObjectMessage<BeeInformation>) nextMessage();
	if (m == null ) {
			return;
	}
	if (m.getSender().getRole().equals("hornet")){
		kill = true;
		Timer timer = new Timer(true);
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
			}
		}, 3000);
		getLogger().info(() -> "Je meurs ");
		killAgent(this);
	}
	else if (m.getSender().equals(leader)) {// leader quitting
	    leader = null;
	    leaderInfo = null;
	}
	else {
	    if (leader == null)
		followNewLeader(m);
	    else {
		List<AgentAddress> queens = getAgentsWithRole(COMMUNITY, SIMU_GROUP, QUEEN_ROLE);
		if (queens != null && generator.nextDouble() < (1.0 / queens.size())) {// change leader randomly
		    followNewLeader(m);
		}
	    }
	}
    }

    /**
     * @param leaderMessage
     */
    private void followNewLeader(ObjectMessage<BeeInformation> leaderMessage) {
	leader = leaderMessage.getSender();
	leaderInfo = leaderMessage.getContent();
	myInformation.setBeeColor(leaderInfo.getBeeColor());
    }

    @Override
    protected void computeNewVelocities() {
		if (kill) { dX=0; dY =0; return;}
	final Point location = myInformation.getCurrentPosition();
	// distances from bee to queen
	int dtx;
	int dty;
	if (leaderInfo != null) {
	    final Point leaderLocation = leaderInfo.getCurrentPosition();
	    dtx = leaderLocation.x - location.x;
	    dty = leaderLocation.y - location.y;
	}
	else {
	    dtx = generator.nextInt(5);
	    dty = generator.nextInt(5);
	    if (generator.nextBoolean()) {
		dtx = -dtx;
		dty = -dty;
	    }
	}
	int acc = 0;
	if (beeWorld != null) {
	    acc = beeWorld.getBeeAcceleration().getValue();
	}
	int dist = Math.abs(dtx) + Math.abs(dty);
	if (dist == 0)
	    dist = 1; // avoid dividing by zero
	// the randomFromRange adds some extra jitter to prevent the bees from flying in formation
	dX += ((dtx * acc) / dist) + randomFromRange(2);
	dY += ((dty * acc) / dist) + randomFromRange(2);
    }

    @Override
    protected int getMaxVelocity() {
	if (beeWorld != null) {
	    return beeWorld.getBeeVelocity().getValue();
	}
	return 0;
    }

	@Override
	protected void end() {
		broadcastMessage(COMMUNITY, SIMU_GROUP, "hornet", new ObjectMessage<>(myInformation));
	}
}
