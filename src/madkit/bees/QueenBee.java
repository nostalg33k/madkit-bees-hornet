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


import static java.lang.Thread.sleep;
import static madkit.bees.BeeLauncher.*;

import java.awt.Point;

import madkit.bees.AbstractBee;
import madkit.kernel.Message;
import madkit.message.ObjectMessage;
import madkit.message.StringMessage;

/**
 * The leader of a group.
 * 
 * @version 2.0.0.3
 * @author Fabien Michel, Olivier Gutknecht
 */
public class QueenBee extends AbstractBee {

    /**
     * 
     */
    private static final long serialVersionUID = -6999130646300839798L;
    static int border = 20;

    @Override
    protected void buzz() {
	Message m = nextMessage();

	if (m != null) {
		if (m.getSender().getRole().equals("hornet")) {
			getLogger().info(() -> "Je meurs ");
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			killAgent(this);
		}
		else {
			sendReply(m, new ObjectMessage<>(myInformation));
		}
	}

	super.buzz();


		if (beeWorld != null) {
			// check to see if the queen hits the edge
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


    @Override
    protected void activate() {
	requestRole(COMMUNITY, SIMU_GROUP, QUEEN_ROLE, null);
	requestRole(COMMUNITY, SIMU_GROUP, BEE_ROLE, null);
	requestRole(COMMUNITY, SIMU_GROUP, PREY, null);
	broadcastMessage(COMMUNITY, SIMU_GROUP, "hornet", new ObjectMessage<>(myInformation));
	broadcastMessage(COMMUNITY, SIMU_GROUP, FOLLOWER_ROLE, new ObjectMessage<>(myInformation));
    }

    @Override
    protected void end() {
		broadcastMessage(COMMUNITY, SIMU_GROUP, FOLLOWER_ROLE, new ObjectMessage<>(myInformation));
    }

    @Override
    protected int getMaxVelocity() {
	if (beeWorld != null) {
	    return beeWorld.getQueenVelocity().getValue();
	}
	return 0;
    }

    @Override
    protected void computeNewVelocities() {
	if (beeWorld != null) {
	    int acc = beeWorld.getQueenAcceleration().getValue();
	    dX += randomFromRange(acc);
	    dY += randomFromRange(acc);
	}
    }


}
