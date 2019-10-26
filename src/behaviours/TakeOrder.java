package behaviours;

import agents.Waiter;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.Dish;

public class TakeOrder extends CyclicBehaviour
{
    private static final long serialVersionUID = 7818256748738825651L;
    private int step = 0;
    private MessageTemplate template;
    private int customerMood;
    private Waiter myWaiter = (Waiter) myAgent;

    @Override
    public void action() {
        ACLMessage msg;

        switch(step) {
            //Receive request from customer
            case 0:
                //Other protocol (cfp ?): Waiter - customer
                template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST), //TODO Change performative
                    MessageTemplate.MatchConversationId("order-request"));
                msg = myAgent.receive(template);

                if(msg != null) 
                    attendCustomer(msg);
                else
                    block();

                break;

            case 1:
                template = MessageTemplate.MatchConversationId("dish-details");
                msg = myAgent.receive(template);

                if(msg != null) 
                    receiveDishDetails(msg);
                else
                    block();

                break;
                //FIPA-REQUEST: waiter - waiter, waiter - kitchen
            default:
                return;
        }
    }

    private void receiveDishDetails(ACLMessage msg) {
        String content = msg.getContent();
        String[] dishDetails = content.split(" "); //Message format: "dish availability cookigTime preparationRate"
        Dish dish = new Dish(dishDetails[0], Integer.parseInt(dishDetails[1]), Integer.parseInt(dishDetails[2]), 
            Integer.parseInt(dishDetails[3]), msg.getSender().getName().equals(myWaiter.getName()));
            
        if(myWaiter.getKnownDishes().contains(dish)) 
            myWaiter.updateKnowDish(dish);
        else
            myWaiter.getKnownDishes().add(dish);

        //Customer mood += cookingTime - 15 | Customer mood += DishPreparation - 5
        if(Integer.parseInt(dishDetails[1]) == 0 
        || customerMood + (Integer.parseInt(dishDetails[2]) - 15) <= 3 
        || customerMood + (Integer.parseInt(dishDetails[3]) - 5) <= 3) {
            //TODO Suggest something else (based on known dishes ?)
        }
        else {
            //TODO Relay request to kitchen
            //TODO Deliver food
        }

        step = 2;
    }

    private void attendCustomer(ACLMessage msg) {
       
        ACLMessage reply = msg.createReply();

        if(myWaiter.isBusy()) {
            reply.setPerformative(ACLMessage.REFUSE);
            reply.setContent("busy");
            myAgent.send(reply);
            return;
        }
        else
            myWaiter.addCustomer(msg.getSender());

        String[] customerDetails = msg.getContent().split(" "); //Message: <Dish Mood>
        //TODO Differentiate dish from quickest-dish request (?)
        String dish = customerDetails[0];
        customerMood = Integer.parseInt(customerDetails[1]);

        //TODO Drop one point to customer mood if asking kitchen
        if(customerMood <= 5) { 
            //TODO Ask waiter
        } 
        else {
            ACLMessage kitchenRequest = new ACLMessage(ACLMessage.REQUEST);
            kitchenRequest.addReceiver(myWaiter.getKitchen());
            kitchenRequest.setConversationId("dish-details");
            kitchenRequest.setContent(dish);
            myAgent.send(kitchenRequest);
        }
        
        step = 1;
    }
}