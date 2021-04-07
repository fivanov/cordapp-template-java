package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.TemplateContract;
import com.template.states.IOUState;
import net.corda.core.contracts.Command;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class IOUFlow extends FlowLogic<Void> {

    private int iouValue;
    private Party otherParty;

    // We will not use these ProgressTracker for this Hello-World sample
    private final ProgressTracker progressTracker = new ProgressTracker();
    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    //public constructor
    public IOUFlow(int iouValue, Party otherParty)
    {
        this.iouValue = iouValue;
        this.otherParty = otherParty;
    }

    @Suspendable
    @Override
    public Void call() throws FlowException {

        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

        IOUState state = new IOUState(iouValue,getOurIdentity(),otherParty);
        Command command = new Command<>(new TemplateContract.Commands.Send(), getOurIdentity().getOwningKey());

        TransactionBuilder txBuilder = new TransactionBuilder(notary).addOutputState(state,TemplateContract.ID).addCommand(command);
        SignedTransaction signedTX = getServiceHub().signInitialTransaction(txBuilder);
        FlowSession otherPartySession = initiateFlow(otherParty);
        subFlow(new FinalityFlow(signedTX,otherPartySession));

        return null;
    }
}
