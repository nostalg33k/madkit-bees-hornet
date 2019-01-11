package madkit.bees;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.logging.Level;

import com.sun.org.apache.bcel.internal.generic.RETURN;
import madkit.bees.AbstractBee;
import madkit.kernel.AbstractAgent;
import madkit.kernel.AgentAddress;
import madkit.kernel.Message;
import madkit.message.ObjectMessage;

import madkit.bees.BeeEnvironment.*;
import madkit.message.StringMessage;

import static java.lang.StrictMath.abs;

import static java.lang.Thread.sleep;
import static madkit.bees.BeeLauncher.*;

public class Hornet extends AbstractBee {

    static int border = 20;
    BeeInformation preyInfo = null;
    AgentAddress prey = null;
    HashMap<AgentAddress,BeeInformation> preys = new HashMap<>();


    public void activate() {
        requestRole(COMMUNITY, SIMU_GROUP, "hornet", null);
        requestRole(COMMUNITY, SIMU_GROUP, BEE_ROLE, null);
        //requestRole(COMMUNITY, SIMU_GROUP, FOLLOWER_ROLE, null);
        myInformation.setBeeColor(Color.red);
        //broadcastMessage(COMMUNITY, SIMU_GROUP, "prey", new ObjectMessage<>(myInformation));
    }

    @Override
    public void buzz() {
        getPreys();

        if (prey == null) {
            try {
                updatePrey();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        else {
            Point preyLocation = preyInfo.getCurrentPosition();
            Point location = myInformation.getCurrentPosition();
            if ( (abs(preyLocation.y - location.y) < 2) || (abs(preyLocation.x - location.x) < 2) ) {
                try {
                    killPrey(prey);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

        super.buzz();


        if (beeWorld != null) {
            // check to see if the hornet hits the edge
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
    protected int getMaxVelocity() {
        if (beeWorld != null) {
            return beeWorld.getHornetVelocity().getValue();
        }
        return 0;
    }

    @Override
    protected void computeNewVelocities() {
        if (beeWorld != null) {
            int acc = beeWorld.getHornetAcceleration().getValue();
            dX += randomFromRange(acc);
            dY += randomFromRange(acc);
        }

        final Point location = myInformation.getCurrentPosition();
        // distances from hornet to prey
        int dtx;
        int dty;
        if (preyInfo != null) {
            final Point preyLocation = preyInfo.getCurrentPosition();
            dtx = preyLocation.x - location.x;
            dty = preyLocation.y - location.y;
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
            acc = beeWorld.getHornetAcceleration().getValue();
        }
        int dist = Math.abs(dtx) + Math.abs(dty);
        if (dist == 0)
            dist = 1; // avoid dividing by zero
        // the randomFromRange adds some extra jitter to prevent the bees from flying in formation
        dX += ((dtx * acc) / dist) + randomFromRange(2);
        dY += ((dty * acc) / dist) + randomFromRange(2);
    }

    private void followNewPrey(AgentAddress a ,BeeInformation b) throws InterruptedException {
        preyInfo = b;
        prey = a;
    }

    private void getPreys(){
        //localisation des abeilles
       ObjectMessage<BeeInformation> m = (ObjectMessage<BeeInformation>) nextMessage();
        if (m == null) {
            return;
        }
        //Je localise les abeilles
        getLogger().info(() -> "J'ai détecté 1 proie "+ m.getSender().getRole() + m.getContent());

        if (m.getSender().getRole().equals("prey")) {

            preys.put(m.getSender(),m.getContent());
        }
    }

    private void updatePrey() throws InterruptedException {

        Set<Map.Entry<AgentAddress,BeeInformation>> setHm = preys.entrySet();
        Iterator<Map.Entry<AgentAddress,BeeInformation>> it = setHm.iterator();

        Point prey1Location = null;
        final Point location = myInformation.getCurrentPosition();

        Map.Entry<AgentAddress,BeeInformation> bee = null;
        int beeDistance;
        int minDistance = 0;
        AgentAddress closestBee = null;

        //Je selectionne une abeille avec la plus petite distance
        while(it.hasNext()){

            if (closestBee == null) {
                bee = it.next();
                closestBee = bee.getKey();
                prey1Location = bee.getValue().getCurrentPosition();
                minDistance = Integer.min( prey1Location.x - location.x,prey1Location.y - location.y);
            }

            else {
                bee = it.next();
                prey1Location = bee.getValue().getCurrentPosition();
                beeDistance = Integer.min(prey1Location.x - location.x, prey1Location.y - location.y);

                if (beeDistance < minDistance) {
                    closestBee = bee.getKey();
                    minDistance = beeDistance;
                }
            }

        }

        prey = closestBee;
        preyInfo = preys.get(closestBee);

        //Je la follow jusqu'à la mort

        followNewPrey(prey,preyInfo);

    }

    private void killPrey(AgentAddress bee) throws InterruptedException {
        if (sendMessageWithRole(bee, new ObjectMessage<>(myInformation),"hornet") == ReturnCode.SUCCESS){
            getLogger().info(() -> "Je tue ");
            preys.remove(bee);
            prey = null;
            preyInfo = null;
        }
        else {
            getLogger().info(() -> "Pas tuée ");
        }

    }

}


