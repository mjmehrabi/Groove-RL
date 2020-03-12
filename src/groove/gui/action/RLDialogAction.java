package groove.gui.action;

import groove.grammar.model.GrammarModel;
import groove.gui.Options;
import groove.gui.Simulator;
import groove.gui.dialog.RLDialog;

/**
 * @author Mohammad Javad Mehrabi
 * Action to open the Exploration Dialog.
 */

public class RLDialogAction extends SimulatorAction {
    /** Constructs an instance of the action. */
    public RLDialogAction(Simulator simulator) {
        super(simulator, Options.ReachabilityRL_DIALOG_ACTION_NAME,
                null);
    }

    @Override
    public void execute() {
        new RLDialog(getSimulator(), getFrame());

    }



    @Override
    public void refresh() {
        GrammarModel grammar = getSimulatorModel().getGrammar();
        setEnabled(grammar != null && !grammar.hasErrors()
                && grammar.hasRules());
    }
}
