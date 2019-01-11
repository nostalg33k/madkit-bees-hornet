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

import static madkit.bees.BeeLauncher.BEE_ROLE;
import static madkit.bees.BeeLauncher.COMMUNITY;
import static madkit.bees.BeeLauncher.SCHEDULER_ROLE;
import static madkit.bees.BeeLauncher.SIMU_GROUP;

import madkit.action.SchedulingAction;
import madkit.bees.AbstractBee;
import madkit.kernel.AbstractAgent;
import madkit.kernel.Message;
import madkit.message.ObjectMessage;
import madkit.message.SchedulingMessage;
import madkit.simulation.activator.GenericBehaviorActivator;

/**
 * @version 2.0.0.2
 * @author Fabien Michel, Olivier Gutknecht
 */
public class BeeScheduler extends madkit.kernel.Scheduler {

    private GenericBehaviorActivator<AbstractBee> bees;

    @Override
    public void activate() {
	super.activate();
	requestRole(COMMUNITY, SIMU_GROUP, SCHEDULER_ROLE);
	bees = new GenericBehaviorActivator<>(COMMUNITY, SIMU_GROUP, BEE_ROLE, "buzz");
	addActivator(bees);
	GenericBehaviorActivator<AbstractAgent> viewer = new GenericBehaviorActivator<>(COMMUNITY, SIMU_GROUP, "bee observer", "observe");
	addActivator(viewer);
	// auto starting myself the agent way
	receiveMessage(new SchedulingMessage(SchedulingAction.RUN));
    }

    /**
     * Overriding just for adding the multicore option
     * 
     * @see madkit.kernel.Scheduler#checkMail(Message)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void checkMail(Message m) {
	if (m != null) {
	    try {
		boolean mutiCore = ((ObjectMessage<Boolean>) m).getContent();
		if (mutiCore) {
		    bees.useMulticore(Runtime.getRuntime().availableProcessors());
		}
		else {
		    bees.useMulticore(1);
		}
	    }
	    catch(ClassCastException e) {
		super.checkMail(m);// default behavior
	    }
	}
    }

}
