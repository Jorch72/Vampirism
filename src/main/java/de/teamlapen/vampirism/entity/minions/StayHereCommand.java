package de.teamlapen.vampirism.entity.minions;

import de.teamlapen.vampirism.entity.ai.EntityAIStayHere;

public class StayHereCommand extends DefaultMinionCommand {
	protected final EntityRemoteVampireMinion minion;
	protected final EntityAIStayHere stay;

	public StayHereCommand(int id, EntityRemoteVampireMinion minion) {
		super(id);
		this.minion = minion;
		stay = new EntityAIStayHere(minion,4,-1);
	}

	@Override
	public int getMinU() {
		return 80;
	}

	@Override
	public int getMinV() {
		return 0;
	}

	@Override
	public String getUnlocalizedName() {
		return "minioncommand.vampirism.stayhere";
	}

	@Override
	public void onActivated() {
		minion.tasks.addTask(3, stay);

	}

	@Override
	public void onDeactivated() {
		minion.tasks.removeTask(stay);

	}

}
