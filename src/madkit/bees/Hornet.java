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

    private static int border = 0;
    private BeeInformation preyInfo = null;
    private AgentAddress prey = null;
    private HashMap<AgentAddress,BeeInformation> preys = new HashMap<>();
    private boolean kill = false;
    private boolean attack = true;

    private static final int ATTACK_DISTANCE = 2;
    private static final int NOATTACK_NB_OF_BEES = 4;
    private static final int KILLED_DISTANCE = 20;
    private static final int KILLED_NB_OF_BEES = 7;


    public void activate() {
        requestRole(COMMUNITY, SIMU_GROUP, "hornet", null);
        requestRole(COMMUNITY, SIMU_GROUP, BEE_ROLE, null);
        myInformation.setBeeColor(Color.red);
    }

    @Override
    public void buzz() {
        getPreys();

        if (prey == null && preyInfo == null) {
            try {
                updatePrey();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //si l'abeille sort du champ on ne la suit plus
        else if (!isVisible(preyInfo,border)) {
            prey = null;
            preyInfo = null;
        }

        else {
            Point preyLocation = preyInfo.getCurrentPosition();
            Point location = myInformation.getCurrentPosition();
            if ( preyLocation.distance( location.x,location.y) < ATTACK_DISTANCE && attack ) {
                    killPrey(prey);
                    kill = false;
            }
        }

        super.buzz();
        getKilled();

        stayVisible(border);

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
        if (kill) { dX=0; dY =0;  return;}
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
        if (m.getSender().getRole().equals("prey")) {
            getLogger().info(() -> "J'ai détecté 1 proie "+ m.getSender().toString());
            preys.put(m.getSender(),m.getContent());
        }
    }

    private void updatePrey() throws InterruptedException {

        Set<Map.Entry<AgentAddress,BeeInformation>> setHm = preys.entrySet();
        Iterator<Map.Entry<AgentAddress,BeeInformation>> it = setHm.iterator();

        Point prey1Location;
        final Point location = myInformation.getCurrentPosition();

        Map.Entry<AgentAddress,BeeInformation> bee;
        double beeDistance;
        double minDistance = 0;
        AgentAddress closestBee = null;

        //Je selectionne une abeille avec la plus petite distance
        while(it.hasNext()){

            if (closestBee == null) {
                bee = it.next();
                closestBee = bee.getKey();
                prey1Location = bee.getValue().getCurrentPosition();
                minDistance = prey1Location.distance( location.x,location.y);
            }

            else {
                bee = it.next();
                prey1Location = bee.getValue().getCurrentPosition();
                beeDistance = prey1Location.distance( location.x,location.y);

                if (beeDistance < minDistance) {
                    closestBee = bee.getKey();
                    minDistance = beeDistance;
                }
            }

        }
        //Je la follow jusqu'à la mort

        followNewPrey(closestBee,preys.get(closestBee));

    }

    private void killPrey(AgentAddress bee) {
        if (sendMessageWithRole(bee, new ObjectMessage<>(myInformation),"hornet") == ReturnCode.SUCCESS){
            kill = true;
            Timer timer = new Timer(true);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    getLogger().info(() -> "Je tue ");
                    preys.remove(bee);
                    prey = null;
                    preyInfo = null;
                    kill = false;
                }
            }, 3000);

        }

    }

    private void getKilled() {
        Set<Map.Entry<AgentAddress,BeeInformation>> setHm = preys.entrySet();
        Iterator<Map.Entry<AgentAddress,BeeInformation>> it = setHm.iterator();

        Point preyLocation;
        final Point location = myInformation.getCurrentPosition();
        Map.Entry<AgentAddress,BeeInformation> bee;

        int i = 0;

        while(it.hasNext())
        {
            bee = it.next();
            preyLocation = bee.getValue().getCurrentPosition();

            if ( preyLocation.distance( location.x,location.y) < KILLED_DISTANCE){
                i++;
            }

            attack = (i < NOATTACK_NB_OF_BEES);

        }

        if (i >= KILLED_NB_OF_BEES){
            kill = true;
            Timer timer = new Timer(true);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    getLogger().info(() -> "Je meurs ");
                    killAgent(Hornet.this);
                }
            }, 5000);

        }

    }

}


