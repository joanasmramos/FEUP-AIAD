package agents;

import java.util.*;

import behaviours.OrderPerformer;
import behaviours.ServiceSearch;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.SimpleAchieveREInitiator;

public class Customer extends RestaurantAgent {
    private static final long serialVersionUID = 3921787877132989337L;
    private String desiredDish;
    private boolean hasDishArgument;
    private HashSet<AID> unavailableWaiters = new HashSet<>();
    private boolean hasWaiter;
    private AID waiter;
    private ArrayList<AID> waiters = new ArrayList<>();
    private int mood;
    private int initialMood;
    private int attempts;
    private ServiceSearch serviceSearch;

    @Override
    protected void setup() {
        role = "Customer";
        
        printMessage("Hello! Customer " + getAID().getLocalName() + " is ready.");

        Random random = new Random();
        mood = random.nextInt(9) + 1; //10 being very relaxed and 1 being very frustrated
        initialMood = mood;

        printMessage("My mood: " + mood);

        hasWaiter = false;
        attempts = 0;
        desiredDish = "";
        hasDishArgument = false;

        Object[] args = getArguments();

        if(args.length > 0) {
            desiredDish = (String) args[0];
            hasDishArgument = true;
        }

        serviceSearch = new ServiceSearch(this, 1000);
        addBehaviour(serviceSearch);
    }

    @Override
    public void addWaiters(AID[] newWaiters) {
        this.setWaiters(new ArrayList<>(Arrays.asList(newWaiters)));

        if(!this.hasWaiter()) {
            this.getAvailableWaiter();
        }
    }

    private void setWaiters(ArrayList<AID> agents) {
        waiters = agents;
    }

    private boolean hasWaiter() {
        return hasWaiter;
    }

    public int getAttempts() {
        return attempts;
    }

    public void incrementAttempts() {
        attempts++;
    }

    public String getDesiredDish() {
        return desiredDish;
    }

    public AID getWaiter() {
        return waiter;
    }

    public int getMood() {
        return mood;
    }

    public int getInitialMood() {
        return initialMood;
    }

    public void decrementMood() {
        if (mood > 0) {
            mood--;
        }
    }

    public void incrementMood() {
        if (mood < 10) {
            mood++;
        }
    }

    private AID getCurrentWaiter() {
        Random random = new Random();
        int index = random.nextInt(waiters.size());

        boolean allWaitersUnavailable = true;

        for (AID aid : waiters) {
            if(!unavailableWaiters.contains(aid)) {
                allWaitersUnavailable = false;
            }
        }

        if(allWaitersUnavailable) {
            return null;
        }

        while (unavailableWaiters.contains(waiters.get(index))) {
            index = random.nextInt(waiters.size());
        }
        return waiters.get(index);
    }

    // Step 0: Find an available waiter (corresponds to waiter's step 0)
    private void getAvailableWaiter() {
        AID currentWaiter = getCurrentWaiter();

        if(currentWaiter == null) {
            printMessage("There isn't an available waiter :(");
            doDelete();
            return;
        }

        // get an available waiter
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        msg.addReceiver(currentWaiter);
        msg.setConversationId("waiter-request");
        msg.setContent("Be my waiter " + currentWaiter.getLocalName());

        printMessage("Are you available, " + currentWaiter.getLocalName() + "?");

        addBehaviour(new SimpleAchieveREInitiator(this, msg) {
            @Override
            protected void handleInform(ACLMessage inform) {
                hasWaiter = true;
                waiter = currentWaiter;
                serviceSearch.stop();
                orderDish();
            }

            @Override
            protected void handleRefuse(ACLMessage msg) {
                unavailableWaiters.add(currentWaiter);
            }
        });
    }

    private void decideDish() {
        String oldDish = desiredDish;
        String[] dishes = Kitchen.getMenu();
        Random rand = new Random();

        desiredDish = dishes[rand.nextInt(dishes.length)];

        while(oldDish.equals(desiredDish)) {
            desiredDish = dishes[rand.nextInt(dishes.length)];
        }
    }

    // Step 1: Order dish (corresponds to waiter's steps 1 and 3)
    public void orderDish() {
        if(!hasDishArgument) {
            decideDish();
        }
        else {
            hasDishArgument = false;
        }

        printMessage("I would like to eat " + desiredDish + ".");

        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        msg.setLanguage("English");
        msg.addReceiver(waiter);
        msg.setConversationId("order-request");
        msg.setContent(desiredDish + " - " + mood); // Message: <Dish - Mood>

        addBehaviour(new OrderPerformer(this, msg));
    }

    @Override
    protected void takeDown() {
        printMessage("My mood: " + mood);
        printMessage("Going home");
    }

}
