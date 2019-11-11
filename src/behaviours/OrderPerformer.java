package behaviours;

import agents.Customer;
import jade.lang.acl.ACLMessage;
import jade.proto.SimpleAchieveREInitiator;

public class OrderPerformer extends SimpleAchieveREInitiator {
    private static final long serialVersionUID = 2897989135282380056L;
    private Customer customer;

    public OrderPerformer(Customer c, ACLMessage msg) {
        super(c, msg);
        customer = c;
    }

    @Override
    protected void handleAgree(ACLMessage agree) {
        customer.printMessage("Waiting for my dish.");
    }

}
