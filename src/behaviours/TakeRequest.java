package behaviours;

import agents.Kitchen;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class TakeRequest extends CyclicBehaviour{

    private static final long serialVersionUID = 3055341223034464997L;
    private Kitchen myKitchen;


    public TakeRequest(Kitchen kitchen) {
        myKitchen = kitchen;
    }

    @Override
    public void action() {
        ACLMessage request = myKitchen.receive();

        if(request != null) {
            String convId = request.getConversationId();
            ACLMessage reply = request.createReply();
            String meal = request.getContent();
            String mealString = "";

            reply.setConversationId(request.getConversationId());
            reply.setPerformative(ACLMessage.AGREE);
            reply.setContent("ok");
            myKitchen.send(reply);

            if(myKitchen.checkMeal(meal)) {
                int[] mealInfo = myKitchen.getMealInfo(meal);
                mealString = meal + " - " + mealInfo[0] + " - " + mealInfo[1];
                if(convId.equals("dish-details"))
                    mealString += " - " + mealInfo[2];
                reply.setContent(mealString);
                reply.setPerformative(ACLMessage.INFORM);
            } else {
                reply.setContent(meal);
                reply.setPerformative(ACLMessage.FAILURE);
            }
            myKitchen.send(reply);
        }
        else
            block();
    }
}