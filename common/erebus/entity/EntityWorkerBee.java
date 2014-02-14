package erebus.entity;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import erebus.ModItems;
import erebus.client.render.entity.AnimationMathHelper;
import erebus.entity.ai.EntityAIPolinate;
import erebus.item.ItemErebusMaterial;

public class EntityWorkerBee extends EntityAnimal {
	public ChunkCoordinates currentFlightTarget;
	public float wingFloat;
	private final AnimationMathHelper mathWings = new AnimationMathHelper();
	public boolean beeFlying;
	public boolean beePollinating;

	public EntityWorkerBee(World world) {
		super(world);
		setSize(1.5F, 1.0F);
		tasks.addTask(0, new EntityAIPolinate(this, 10));
		tasks.addTask(1, new EntityAISwimming(this));
		tasks.addTask(2, new EntityAIAttackOnCollide(this, 0.5D, true));
		tasks.addTask(3, new EntityAITempt(this, 0.5D, Item.sugar.itemID, false));
		tasks.addTask(5, new EntityAIWander(this, 0.4D));
		tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
		tasks.addTask(7, new EntityAILookIdle(this));
		targetTasks.addTask(0, new EntityAIHurtByTarget(this, true));
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataWatcher.addObject(22, new Integer(0));
	}
	
	@Override
	public boolean isAIEnabled() {
		return true;
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.75D);
		getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(25.0D);
	}

	@Override
	public EnumCreatureAttribute getCreatureAttribute() {
		return EnumCreatureAttribute.ARTHROPOD;
	}

	@Override
	protected void fall(float par1) {
	}

	@Override
	protected String getLivingSound() {
		return "erebus:WaspSound";
	}

	@Override
	protected String getHurtSound() {
		return "erebus:WaspHurt";
	}

	@Override
	protected String getDeathSound() {
		return "erebus:squish";
	}

	@Override
	protected void playStepSound(int x, int y, int z, int blockID) {
		playSound("mob.spider.step", 0.15F, 1.0F);
	}

	@Override
	protected void dropFewItems(boolean recentlyHit, int looting) {
		entityDropItem(new ItemStack(ModItems.erebusMaterials, getNectarPoints(), ItemErebusMaterial.dataNectar), 0.0F);
		entityDropItem(new ItemStack(ModItems.erebusMaterials, rand.nextInt(3) + 1, ItemErebusMaterial.dataExoPlate), 0.0F);
	}

	public boolean isFlying() {
		return !onGround;
	}

	@Override
	public void onUpdate() {
		if (!isFlying())
			wingFloat = 0.0F;
		else
			wingFloat = mathWings.swing(4.0F, 0.1F);

		if (motionY < 0.0D)
			motionY *= 0.5D;

		if (!worldObj.isRemote)
			if (rand.nextInt(200) == 0 && !beePollinating)
				if (!beeFlying)
					setBeeFlying(true);
				else
					setBeeFlying(false);

		if (!worldObj.isRemote) {
			if (beeFlying && !beePollinating)
				flyAbout();
			else
				land();
		}
		super.onUpdate();
	}

	public void setBeeFlying(boolean state) {
		beeFlying = state;
	}
	
	public void setBeePollinating(boolean state) {
		beePollinating = state;
	}

	public void flyAbout() {
		if (currentFlightTarget != null && (!worldObj.isAirBlock(currentFlightTarget.posX, currentFlightTarget.posY, currentFlightTarget.posZ) || currentFlightTarget.posY < 1))
			currentFlightTarget = null;

		if (currentFlightTarget == null || rand.nextInt(30) == 0 || currentFlightTarget.getDistanceSquared((int) posX, (int) posY, (int) posZ) < 10.0F)
			currentFlightTarget = new ChunkCoordinates((int) posX + rand.nextInt(7) - rand.nextInt(7), (int) posY + rand.nextInt(6) - 2, (int) posZ + rand.nextInt(7) - rand.nextInt(7));
		flyToTarget();
	}
	
	public void flyToTarget() {
		if (currentFlightTarget != null && worldObj.getBlockId(currentFlightTarget.posX, currentFlightTarget.posY, currentFlightTarget.posZ)==170 && isCollidedHorizontally && worldObj.isAirBlock(currentFlightTarget.posX, currentFlightTarget.posY+1, currentFlightTarget.posZ)){
			this.setPosition(currentFlightTarget.posX, currentFlightTarget.posY+1, currentFlightTarget.posZ);
			this.playSound("mob.endermen.portal", 1.0F, 1.0F);
			}
		else {
			double var1 = currentFlightTarget.posX + 0.5D - posX;
			double var3 = currentFlightTarget.posY + 0.1D - posY;
			double var5 = currentFlightTarget.posZ + 0.5D - posZ;
			motionX += (Math.signum(var1) * 0.5D - motionX) * 0.10000000149011612D;
			motionY += (Math.signum(var3) * 0.699999988079071D - motionY) * 0.10000000149011612D;
			motionZ += (Math.signum(var5) * 0.5D - motionZ) * 0.10000000149011612D;
			float var7 = (float) (Math.atan2(motionZ, motionX) * 180.0D / Math.PI) - 90.0F;
			float var8 = MathHelper.wrapAngleTo180_float(var7 - rotationYaw);
			moveForward = 0.5F;
			rotationYaw += var8;
		}
	}

	private void land() {
		//Nothing to see here - yet
	}
	
	@Override
	public boolean interact(EntityPlayer player) {
		ItemStack is = player.inventory.getCurrentItem();
		if (!worldObj.isRemote && is != null && is.itemID == Item.sugar.itemID && getNectarPoints()>0) {
			entityDropItem(new ItemStack(ModItems.erebusMaterials, 1, ItemErebusMaterial.dataNectar), 0.0F);
			--is.stackSize;
			setNectarPoints(getNectarPoints()-1);
			return true;
		}
		return super.interact(player);
	}
	
	public void setNectarPoints(int count) {
		dataWatcher.updateObject(22, Integer.valueOf(count));	
	}
	
	public int getNectarPoints() {
		return dataWatcher.getWatchableObjectInt(22);
	}

	@Override
	public EntityAgeable createChild(EntityAgeable entityageable) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setInteger("nectarPoints", getNectarPoints());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		setNectarPoints(nbt.getInteger("nectarPoints"));
	}
}