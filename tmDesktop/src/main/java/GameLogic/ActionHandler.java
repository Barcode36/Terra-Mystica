package GameLogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class ActionHandler {

	private Player currentPlayer;
	private static ActionHandler instance = new ActionHandler();

	/* These variables will be passed as arguments to the actions. */
	final int NUMBER_OF_ACTIONS = 8;
	final int NUMBER_OF_POWER_ACTIONS = 6;
	private int terrainXPosition;
	private int terrainYPosition;
	private int terrainTypeIndex;
	private ArrayList<Terrain> terrainWithSameType = new ArrayList<>();
	private Structure structureToBuild;
	private int actionId;
	private Cult cultType;
	private int priestPosition;
	private boolean[] performableActions = new boolean[NUMBER_OF_ACTIONS];
	private boolean[] powerActionPerformed = new boolean[NUMBER_OF_POWER_ACTIONS];
	private int townTileId;
	private int favorTileId;
	private int bonusCardId;
	private int totalAdjacentBuildingPower;
	private TerrainType targetTerrainType;
	private int actionIndex;
	private Terrain terrainToBeModified;
	/*
	 * The controller will set the values of these variables with its setter
	 * methods.
	 */
	// ACCESSOR AND MUTATOR FUNCTIONS
	public int getTotalAdjacentBuildingPower(){
		return this.totalAdjacentBuildingPower;
	}

	public void setBonusCardId(int id){
		this.bonusCardId = id;
	}

	public int getBonusCardId(){
		return this.bonusCardId;
	}

	public void setTownTileId(int id){
		townTileId = id;
	}

	public int getTownTileId(){
		return townTileId;
	}
	
	public void setCultType(Cult c) {
		cultType = c;
	}

	public void setPriestPosition(int pos) {
		priestPosition = pos;
	}

	public void setActionID(int id) {
		actionId = id;
	}

	public int getActionID() {
		return actionId;
	}

	public void setStructureToBuild(Structure s) {
		structureToBuild = s;
	}

	public static ActionHandler getInstance() {
		return instance;
	}

	public int getTerrainXPosition() {
		return this.terrainXPosition;
	}

	public void setTerrainXPosition(int x) {
		terrainXPosition = x;
	}

	public int getTerrainYPosition() {
		return this.terrainYPosition;
	}

	public void setTerrainYPosition(int y) {
		terrainYPosition = y;
	}

	public int getTerrainTypeIndex() {
		return terrainTypeIndex;
	}

	public void setTerrainTypeIndex(int index) {
		terrainTypeIndex = index;
	}

	public ArrayList<Terrain> getTerrainWithSameType() {
		return this.terrainWithSameType;
	}

	public void setActionIndex(int id) {
		this.actionIndex = id;
	}
	public boolean getPerformableActionId() { return this.performableActions[actionIndex]; }
	
	/** START OF TERRAFORM AND BUILD */
	/**
	 *
	 */
	private boolean canTerraformTerrain() {
		//Terrain terrainToBeModified = Game.getInstance().getTerrain(terrainXPosition, terrainYPosition);
		System.out.println("***************************terrain to be modified: " + terrainToBeModified.getStructureType());
		if (targetTerrainType == terrainToBeModified.getType()) {
			System.out.println("Cannot terraform to the same type");
			return false;
		} else if (terrainToBeModified.getStructureType() != Structure.EMPTY) { //terrainToBeModified.getOwner() != null
			System.out.println("That terrain has an owner");
			return false;
		} else { // Check if the player has enough assets to terraform
			System.out.println("Target Terrain Type : " + targetTerrainType);
			int spadeCount = currentPlayer.getFaction().getRequiredSpades(terrainToBeModified.getType());
			//--------------
			System.out.println();
			System.out.println("----------spade count: " + spadeCount);
			//--------------------
			//spadeCount -= currentPlayer.getFaction().spadesEarnedFromPowerActions;
			Asset terraformCost = currentPlayer.getFaction().getSpadeCost();

			System.out.println("----------faction asset: " + currentPlayer.getFaction().asset);

			for (int i = 0; i < spadeCount - 1; i++) {
				terraformCost.performIncrementalTransaction(terraformCost);
				System.out.println("----------terraform cost: " + terraformCost);
			}
			System.out.println("----------Terrain to be modified: " + terrainToBeModified.getType());
			System.out.println();

			boolean hasEnoughAssets = currentPlayer.getFaction().asset.canPerformDecrementalTransaction(terraformCost);
			for (int i = 0; i < spadeCount - 1; i++) {
				terraformCost.performDecrementalTransaction(terraformCost);
			}
			return hasEnoughAssets;
		}
	}

	/**
	 *
	 */
	private void terraformAndBuild(Terrain terrainToBeModified) {
		if (canTerraformTerrain()) {
			int spadeCount = currentPlayer.getFaction().getRequiredSpades(targetTerrainType);
			//spadeCount -= currentPlayer.getFaction().spadesEarnedFromPowerActions;


			Asset terraformCost = currentPlayer.getFaction().getSpadeCost();
			// Modify the terraland
			Game.getInstance().modifyTerraland(targetTerrainType, terrainXPosition, terrainYPosition);

			// Perform transaction with the player's asset. Find the spadecCount, there
			// might be need for more than 1 spade

			for (int i = 0; i < spadeCount; i++) {
				currentPlayer.getFaction().getAsset().performDecrementalTransaction(terraformCost);
			}
			System.out.println("Cost of the terraform :" + spadeCount + " x " + terraformCost);
			System.out.println("Player now has : " + currentPlayer.getFaction().asset);
		}
		// Asssume that a player would build a dwelling if possible
		if (canBuildDwelling(terrainXPosition, terrainYPosition)){
			buildDwelling(terrainXPosition, terrainYPosition);
		}
	}

	private boolean canBuildDwelling(int terrainXPosition, int terrainYPosition) {
		Terrain temp = Game.getInstance().getTerrain(terrainXPosition, terrainYPosition);
		if (temp.getStructureType() != Structure.EMPTY || temp.getType() != currentPlayer.getFaction().homeTerrain
				|| currentPlayer.getNumberOfStructures(Structure.DWELLING) == 8) {
			return false;
		} else {
			Asset dwellingCost = currentPlayer.getFaction().costPerStructure.get(Structure.DWELLING);
			return currentPlayer.getFaction().asset.canPerformDecrementalTransaction(dwellingCost);
		}
	}

	/**
	 *
	 * @param terrainXPosition
	 * @param terrainYPosition
	 */
	private void buildDwelling(int terrainXPosition, int terrainYPosition) {
		if (canBuildDwelling(terrainXPosition, terrainYPosition)) {
			// Find the terrain at the given location
			Terrain temp = Game.getInstance().getTerrain(terrainXPosition, terrainYPosition);

			// Perform transcation
			Asset dwellingCost = currentPlayer.getFaction().costPerStructure.get(Structure.DWELLING);
			currentPlayer.getFaction().asset.performDecrementalTransaction(dwellingCost);

			// Add the terrain to the controlled terrains list of the player.
			currentPlayer.getControlledTerrains().add(temp);

			// Set the owner attribute of the terrain
			temp.setOwner(currentPlayer);
			temp.setStructureType(Structure.DWELLING);
			System.out.println("Built " + Structure.DWELLING + " on " + temp);

			// Increment the number of structures for that type
			currentPlayer.updateNumberOfStructures(Structure.DWELLING, 1);
			System.out.println("Number of Dwellings " + currentPlayer.getNumberOfStructures(Structure.DWELLING));
		}
	}

	/** END OF TERRAFORM AND BUILD */

	/** START OF UPGRADE SHIPPING */
	private boolean canUpgradeShipping() {
		// These factions cannot upgrade their shippings
		if (currentPlayer.getFaction().getName().equals("Fakirs")
				|| currentPlayer.getFaction().getName().equals("Dwarves")) {
			return false;
		} else if (currentPlayer.getFaction().shippingLevel == 3) {
			return false;
		} else {
			Asset shippingUpgradeCost = currentPlayer.getFaction().shippingUpgradeCost;
			System.out.println("Cost of the shipping upgrade: " + shippingUpgradeCost);

			return currentPlayer.getFaction().asset.canPerformDecrementalTransaction(shippingUpgradeCost);
		}
	}

	private void upgradeShipping() {
		if (canUpgradeShipping()) {
			// Find the shippingUpgrade cost
			Asset shippingUpgradeCost = currentPlayer.getFaction().shippingUpgradeCost;

			// Perform the transaction
			currentPlayer.getFaction().asset.performDecrementalTransaction(shippingUpgradeCost);
			System.out.println("Player now has : " + currentPlayer.getFaction().asset);

			// Increment the shipping level
			currentPlayer.getFaction().shippingLevel++;
			System.out.println("Current shipping level : " + currentPlayer.getFaction().shippingLevel);

			// Increment player's victoryPoints
			currentPlayer
					.incrementVictoryPoints(currentPlayer.getFaction().getVictoryPointsEarnedWithShippingUpgrade());
		} else {
			System.out.println("Cannot upgrade shipping!");
		}
	}

	/** END OF UPGRADE SHIPPING */

	/** START OF UPGRADE SPADES */
	private boolean canUpgradeSpades() {
		if (currentPlayer.getFaction().name.equals("Darklings")) {
			System.out.println("Darklings cannot upgrade their spade level");
			return false;
		} else if (currentPlayer.getFaction().spadeLevel == 2) {
			System.out.println("Spade level at its max!");
			return false;
		} else {
			Asset spadeUpgradeCost = currentPlayer.getFaction().getSpadeUpgradeCost();
			return currentPlayer.getFaction().asset.canPerformDecrementalTransaction(spadeUpgradeCost);
		}
	}

	private void upgradeSpades() {
		if (canUpgradeSpades()) {
			// Find the cost for spade upgrade
			Asset spadeUpgradeCost = currentPlayer.getFaction().getSpadeUpgradeCost();
			System.out.println("Cost of the spade upgrade: " + spadeUpgradeCost);

			// Perform the transaction
			currentPlayer.getFaction().getAsset().performDecrementalTransaction(spadeUpgradeCost);
			System.out.println("Player now has : " + currentPlayer.getFaction().asset);

			// Increment the spade level
			currentPlayer.getFaction().spadeLevel++;
			System.out.println("Spade Level : " + currentPlayer.getFaction().spadeLevel);

			// Increment player's victoryPoints
			currentPlayer.incrementVictoryPoints(currentPlayer.getFaction().getVictoryPointsEarnedWithSpadeUpgrade());
		} else {
			System.out.println("Cannot upgrade spades");
		}
	}

	/** END OF UPGRADE SPADES */

	/** START OF UPGRADE STRUCTURE */
	private boolean canUpgradeStructure(int terrainXPosition, int terrainYPosition) {
		Terrain positionOnTerraLand = Game.getInstance().getTerrain(terrainXPosition, terrainYPosition);

		Structure structureOnTerrain = positionOnTerraLand.getStructureType();

		Asset upgradeCost = currentPlayer.getFaction().costPerStructure.get(structureToBuild);

		// FOR UPGRADING TO TRADING POST
		if (structureToBuild == Structure.TRADINGPOST && structureOnTerrain == Structure.DWELLING) {
			// Cannot have more than 4 Trading Posts
			if (currentPlayer.getNumberOfStructures(structureToBuild) == 4) {
				return false;
			}
			// Check if there is an adjacent structure
			if (checkDirectlyAdjacentStructure(terrainXPosition, terrainYPosition) == false) {
				// Double the upgrade cost
				upgradeCost.performIncrementalTransaction(
						new Asset(upgradeCost.getCoin(), upgradeCost.getPriest(), upgradeCost.getWorker(), 0));
			}

			if (currentPlayer.getFaction().asset.canPerformDecrementalTransaction(upgradeCost) == true) {
				// Reset the upgrade cost to the originial value
				upgradeCost.performDecrementalTransaction(
						new Asset(upgradeCost.getCoin(), upgradeCost.getPriest(), upgradeCost.getWorker(), 0));
				return true;
			}

			else {
				return false;
			}
		} else if ((structureToBuild == Structure.STRONGHOLD && structureOnTerrain == Structure.TRADINGPOST)
				|| (structureToBuild == Structure.SANCTUARY && structureOnTerrain == Structure.TEMPLE)) {
			// Cannot have more than 1 Stronghold or Sanctuary
			if (currentPlayer.getNumberOfStructures(structureToBuild) == 1) {
				return false;
			} else {
				return currentPlayer.getFaction().asset.canPerformDecrementalTransaction(upgradeCost);
			}
		} else if ((structureToBuild == Structure.TEMPLE && structureOnTerrain == Structure.TRADINGPOST)) {
			// Cannot have more than 3 Temples
			if (currentPlayer.getNumberOfStructures(structureToBuild) == 3) {
				return false;
			} else {
				return currentPlayer.getFaction().asset.canPerformDecrementalTransaction(upgradeCost);
			}
		} else {
			return false;
		}
	}

	private void upgradeStructure(int terrainXPosition, int terrainYPosition) {
		ArrayList<Terrain> visitedTerrains = new ArrayList<>();
		if (canUpgradeStructure(terrainXPosition, terrainYPosition) == true) {

			Terrain positionOnTerraLand = Game.getInstance().getTerrain(terrainXPosition, terrainYPosition);
			Structure structureOnTerrain = positionOnTerraLand.getStructureType();
			Asset upgradeCost = currentPlayer.getFaction().costPerStructure.get(structureToBuild);

			currentPlayer.getFaction().asset.performDecrementalTransaction(upgradeCost);
			// If there isn't an adjacent building of another player nearby of Tradingpost
			// costs twice as much
			if (structureToBuild == Structure.TRADINGPOST
					&& checkDirectlyAdjacentStructure(terrainXPosition, terrainYPosition)) {
				currentPlayer.getFaction().asset.performDecrementalTransaction(upgradeCost);
			}
			if( structureToBuild == Structure.TEMPLE || structureToBuild == Structure.SANCTUARY ){
				currentPlayer.chooseFavorTile(favorTileId);
				//advance on cult board according to selected tile
				Cult cultType = Game.getInstance().getFavorTileDeck()[favorTileId].getCultBonusType();
				int cultAdvance = Game.getInstance().getFavorTileDeck()[favorTileId].getCultMove();
				moveOnCultBoard(currentPlayer,cultType,cultAdvance);
			}
			// Decrease the number of structures for the structure that has been upgraded
			currentPlayer.updateNumberOfStructures(structureOnTerrain, -1);
			// Increase the number of structures for the structure that has been upgraded
			currentPlayer.updateNumberOfStructures(structureToBuild, 1);
			// Change the structure on the Terrain
			positionOnTerraLand.setStructureType(structureToBuild);
		}
		
		calculateAdjacentBuildingPower(terrainXPosition, terrainYPosition, 0, visitedTerrains);
		for (Terrain t : visitedTerrains){
			totalAdjacentBuildingPower += currentPlayer.getFaction().powerPerBuilding.get(t.getStructureType());
		}
		
	}

	public boolean canFoundTown(int totalAdjacentBuildingPower){
		return this.totalAdjacentBuildingPower >= currentPlayer.getRequiredPowerToFoundTown();
	}

	public void calculateAdjacentBuildingPower(int terrainXPosition, int terrainYPosition, int currentPower, ArrayList<Terrain> visitedTerrains) {
		Terrain current = Game.getInstance().getTerrain(terrainXPosition, terrainYPosition);
		System.out.println(terrainXPosition + " " + terrainYPosition);
		//int totalPower = currentPlayer.getFaction().powerPerBuilding.get(current.getStructureType());
		System.out.println(currentPower);
		Terrain temp = current;

		if(visitedTerrains.contains(current)){
			return;
		} 

		else{
			visitedTerrains.add(temp);
		}

		if (terrainXPosition != 0) {
			// (x-1, y)
			temp = Game.getInstance().getTerrain(terrainXPosition - 1, terrainYPosition);
			/**
			 * If one of the buildings is a Sanctuary Only 3 Buildings will be required to
			 * found a town. Incrementing totalPower by an extra 1, would make this
			 * possible.
			 */
			if (temp.getStructureType() == Structure.SANCTUARY) {
				currentPower++;
			}
			if (current.getType() == temp.getType() && temp.getStructureType() != Structure.EMPTY) {
				currentPower += currentPlayer.getFaction().powerPerBuilding.get(temp.getStructureType());
				calculateAdjacentBuildingPower(temp.getX(), temp.getY(), currentPower, visitedTerrains);
			}
			// (x-1,y+1)
			if (terrainYPosition != 12) {
				if (temp.getStructureType() == Structure.SANCTUARY) {
					currentPower++;
				}
				temp = Game.getInstance().getTerrain(terrainXPosition - 1, terrainYPosition + 1);
				if (current.getType() == temp.getType() && temp.getStructureType() != Structure.EMPTY) {
					currentPower += currentPlayer.getFaction().powerPerBuilding.get(temp.getStructureType());
					calculateAdjacentBuildingPower(temp.getX(), temp.getY(), currentPower, visitedTerrains);
				}
			}
		}

		if (terrainYPosition != 0) {
			// (x, y-1)
			temp = Game.getInstance().getTerrain(terrainXPosition, terrainYPosition - 1);
			if (temp.getStructureType() == Structure.SANCTUARY) {
				currentPower++;
			}
			
			if (current.getType() == temp.getType() && temp.getStructureType() != Structure.EMPTY) {
				currentPower += currentPlayer.getFaction().powerPerBuilding.get(temp.getStructureType());
				calculateAdjacentBuildingPower(temp.getX(), temp.getY(), currentPower, visitedTerrains);
			}
			
		}

		if (terrainYPosition != 12) {
			// (x, y+1)
			temp = Game.getInstance().getTerrain(terrainXPosition, terrainYPosition + 1);
			if (temp.getStructureType() == Structure.SANCTUARY) {
				currentPower++;
			}
			
			if (current.getType() == temp.getType() && temp.getStructureType() != Structure.EMPTY) {
				currentPower += currentPlayer.getFaction().powerPerBuilding.get(temp.getStructureType());
				calculateAdjacentBuildingPower(temp.getX(), temp.getY(), currentPower, visitedTerrains);
			}
			
			// (x+1, y+1)
			if (terrainXPosition != 8) {
				temp = Game.getInstance().getTerrain(terrainXPosition + 1, terrainYPosition + 1);
				if (temp.getStructureType() == Structure.SANCTUARY) {
					currentPower++;
				}
				
				if (current.getType() == temp.getType() && temp.getStructureType() != Structure.EMPTY) {
					currentPower += currentPlayer.getFaction().powerPerBuilding.get(temp.getStructureType());
					calculateAdjacentBuildingPower(temp.getX(), temp.getY(), currentPower, visitedTerrains);
				}
			}
		}
		
		// (x+1, y)
		if (terrainXPosition != 8) {
			temp = Game.getInstance().getTerrain(terrainXPosition + 1, terrainYPosition);
			if (temp.getStructureType() == Structure.SANCTUARY) {
				currentPower++;
			}
			
			if (current.getType() == temp.getType() && temp.getStructureType() != Structure.EMPTY) {
				currentPower += currentPlayer.getFaction().powerPerBuilding.get(temp.getStructureType());
				calculateAdjacentBuildingPower(temp.getX(), temp.getY(), currentPower, visitedTerrains);
			}
			
		}
	}

	public void foundTown(int terrainXPosition, int terrainYPosition){
		Terrain temp = Game.getInstance().getTerrain(terrainXPosition, terrainYPosition);
		currentPlayer.getTownCenters().add(temp);
		// Implement Town Tile
		currentPlayer.chooseTownTile(townTileId);
		System.out.println("Found town at location" + terrainXPosition);
		totalAdjacentBuildingPower = 0;
	}

	/**
	 * A terrain at location(x,y) has adjacent terrains on
	 * (x-1,y),(x-1,y+1),(x,y-1),(x,y+1),(x+1,y),(x+1, y+1)
	 * 
	 * @param terrainXPosition
	 * @param terrainYPosition
	 * @return
	 */
	public boolean checkDirectlyAdjacentStructure(int terrainXPosition, int terrainYPosition) {

		Terrain current = Game.getInstance().getTerrain(terrainXPosition, terrainYPosition);
		Terrain temp;
		// Did not check with the owner since the adjacent terrain might have the owner
		// set to null.

		if (terrainXPosition != 0) {
			temp = Game.getInstance().getTerrain(terrainXPosition - 1, terrainYPosition);
			// (x-1,y)
			if (current.getType() != temp.getType() && temp.getStructureType() != Structure.EMPTY) {
				return true;
			}

			if (terrainYPosition != 12) {
				// (x-1,y+1)
				temp = Game.getInstance().getTerrain(terrainXPosition - 1, terrainYPosition + 1);
				if (current.getType() != temp.getType() && temp.getStructureType() != Structure.EMPTY) {
					return true;
				}
			}
		}

		if (terrainYPosition != 0) {
			// (x, y-1)
			temp = Game.getInstance().getTerrain(terrainXPosition, terrainYPosition - 1);
			if (current.getType() != temp.getType() && temp.getStructureType() != Structure.EMPTY) {
				return true;
			}
		}

		if (terrainYPosition != 12) {
			// (x, y+1)
			temp = Game.getInstance().getTerrain(terrainXPosition, terrainYPosition + 1);
			if (current.getType() != temp.getType() && temp.getStructureType() != Structure.EMPTY) {
				return true;
			}
		}

		if (terrainXPosition != 8) {
			// (x+1, y)
			temp = Game.getInstance().getTerrain(terrainXPosition + 1, terrainYPosition);
			if (current.getType() != temp.getType() && temp.getStructureType() != Structure.EMPTY) {
				return true;
			}

			if (terrainYPosition != 12) {
				// (x+1, y+1)
				temp = Game.getInstance().getTerrain(terrainXPosition + 1, terrainYPosition + 1);
				if (current.getType() != temp.getType() && temp.getStructureType() != Structure.EMPTY) {
					return true;
				}
			}
		}
		return false;
	}

	/** END OF UPGRADE STRUCTURE */

	/** START OF SEND PRIEST TO CULT BOARD */
	// TODO: Implement
	private boolean canSendPriestToCultBoard( Cult cultType ) {
		// Check if there is an empty location for that spot
		if( !Game.getInstance().getCultBoard().hasEmptyPriestLocation(cultType)){
			return false;
		}
		// Check if the player has a priest
		if( currentPlayer.getFaction().getAsset().getPriest() == 0){
			return false;
		}
		return true;
	}

	/**
	 * 
	 * @param cultType
	 * @param priestPosition
	 */
	private void sendPriestToCultBoard(Cult cultType, int priestPosition) {

		// Decrement priest no
		currentPlayer.getFaction().getAsset().setPriest(currentPlayer.getFaction().getAsset().getPriest() - 1);
		// Send priest to cult board
		int[] newPriestLocations = Game.getInstance().getCultBoard().getPriestLocations().get(cultType);
		newPriestLocations[priestPosition] = 1;
		System.out.println("Priest is sended: " + newPriestLocations[priestPosition]);
		Game.getInstance().getCultBoard().getPriestLocations().put(cultType, newPriestLocations);

		// Advance on cult board
		moveOnCultBoard(currentPlayer, cultType, priestPosition);
	}

	public void moveOnCultBoard(Player currentPlayer, Cult cultType, int advanceCount) {

		int[] powerLocations = { 0, 0, 0, 1, 0, 2, 0, 2, 0, 0, 3 };

		int currentPosition = currentPlayer.getPositionOnCultBoard().get(cultType);
		System.out.println(" current position of the player on cult board: " + currentPosition);

		for (int i = 0; i < advanceCount; i++) {

			if (currentPosition == 9) {
				for (int j = 0; j < currentPlayer.getTownKeyUsed().size(); j++) {
					if (currentPlayer.getTownKeyUsed().get(j) == false) {
						currentPosition++;
						// INCREMENT POWER
						currentPlayer.getFaction().incrementPower(3);
						// set town key as used
						currentPlayer.getTownKeyUsed().remove(j);
						currentPlayer.getTownKeyUsed().add(j, true);
						break;
					}
					break;
				}
			} else {
				currentPosition++;
				if (powerLocations[currentPosition] != 0) {
					currentPlayer.getFaction().incrementPower(powerLocations[currentPosition]);
				}

			}
		}
		System.out.println(" current position of the player on cult board: " + currentPosition);

		HashMap<Cult, Integer> newCultBoard = currentPlayer.getPositionOnCultBoard();
		newCultBoard.put(cultType, currentPosition);
		currentPlayer.setPositionOnCultBoard(newCultBoard);
	}

	/** END OF SEND PRIEST TO CULT BOARD */

	/** START OF POWER ACTION */
	private boolean canPerformPowerAction(int powerActionID) {
		int powerCost = 0;
		if (powerActionPerformed[powerActionID]) {
			switch (powerActionID) {
				case 0: // Build bridge
					powerCost = 3;
					return currentPlayer.getFaction().powerbowl[2] > powerCost
							&& currentPlayer.getRemainingBridge() > 0;
				case 1: // Get 1 Priest
					powerCost = 3;
					break;
				case 2: // Get 2 workers
					powerCost = 4;
					break;
				case 3: // Get 7 Coins
					powerCost = 4;
					break;
				case 4: // Get 1 free spade
					powerCost = 4;
					break;
				case 5: // Get 2 free spades
					powerCost = 4;
					break;
			}
		}
		return currentPlayer.getFaction().powerbowl[2] > powerCost;
	}

	/**
	 * There are 6 different poewr actions
	 *
	 */
	private void powerAction(int powerActionID) {
		if (canPerformPowerAction(powerActionID)) {
			powerActionPerformed[powerActionID] = true;
			switch (powerActionID) {
				case 0:
					System.out.println("// TODO: Implement build bridge.");
					break;
				case 1:
					currentPlayer.getFaction().incrementPower(-3);
					currentPlayer.getFaction().asset.performIncrementalTransaction(new Asset(0, 1, 0, 0));
					break;
				case 2:
					currentPlayer.getFaction().incrementPower(-4);
					currentPlayer.getFaction().asset.performIncrementalTransaction(new Asset(0, 0, 2, 0));
					break;
				case 3:
					currentPlayer.getFaction().incrementPower(-4);
					currentPlayer.getFaction().asset.performIncrementalTransaction(new Asset(7, 0, 0, 0));
					break;
				case 4:
					currentPlayer.getFaction().incrementPower(-4);
					currentPlayer.getFaction().spadesEarnedFromPowerActions++;
					break;
				case 5:
					currentPlayer.getFaction().incrementPower(-6);
					currentPlayer.getFaction().spadesEarnedFromPowerActions += 2;
					break;
			}
		} else {
			System.out.println("Cannot perform power action");
		}

	}

	/** END OF POWER ACTION */

	/**
	 * 
	 * @param id
	 */
	private void specialAction(int id) {
		// TODO - implement ActionHandler.specialAction
		throw new UnsupportedOperationException();
	}

	private void pass() {
		currentPlayer.setPassed(true);
		currentPlayer.returnBonusCard();
		// TODO: Choose one of the available bonus card
		Random r = new Random();
		int randomBonusCardId = r.nextInt(8);
		currentPlayer.returnBonusCard();
		if ( !Game.getInstance().retrieveBonusCard(randomBonusCardId).isSelected())
			currentPlayer.chooseBonusCard(randomBonusCardId);
		System.out.println(currentPlayer.getName() + " passed");
	}

	private boolean canPerformSpeacialAction() {
		return false;
	}

	public boolean[] showPlayAbleActions() {
		for (int i = 0; i < 8; i++) {
			setActionID(i + 10);
		}
		return performableActions;
	}

	/**
	 * Will be called in the setup phase of the game
	 * Works in the same way as the buildDwelling method, only difference is that it does not make any transactions
	 */
	private void buildSetupDwelling() {
		if (canBuildDwelling(terrainXPosition, terrainYPosition)) {
			// Find the terrain at the given location
			Terrain temp = Game.getInstance().getTerrain(terrainXPosition, terrainYPosition);
			System.out.println("x,y = " + terrainXPosition + "," + terrainYPosition);

			// Add the terrain to the controlled terrains list of the player.
			currentPlayer.getControlledTerrains().add(temp);

			// Set the owner attribute of the terrain
			temp.setOwner(currentPlayer);
			System.out.println("Built " + Structure.DWELLING + " on " + temp);
			temp.setStructureType(Structure.DWELLING);
			// Increment the number of structures for that type
			currentPlayer.updateNumberOfStructures(Structure.DWELLING, 1);
			System.out.println("Number of Dwellings " + currentPlayer.getNumberOfStructures(Structure.DWELLING));
		}
	}

	/**
	 * 
	 * @param p
	 */
	public void setCurrentPlayer(Player p) {
		this.currentPlayer = p;
		System.out.println("Current Player is " + p.getName());
	}

	// TODO: Update case 0 to showPlayableActions and order the case statements.
	/**
	 *
	 */
	public void executeAction() {

		switch (this.actionId) {
			case 0: // Terraform and build
				System.out.println("Terraform and build");
				//TerrainType t = currentPlayer.getFaction().homeTerrain;
				terraformAndBuild(terrainToBeModified);
				break;
			case 1: // Upgrade shipping level
				System.out.println("Upgrade shipping");
				upgradeShipping();
				break;
			case 2: // Upgrade spade level
				System.out.println("Upgrade spades");
				upgradeSpades();
				break;
			case 3: // Upgrade Structure
				System.out.println("Upgrade structure");
				upgradeStructure(terrainXPosition, terrainYPosition);
				setStructureToBuild(Structure.TRADINGPOST);
				break;
			case 4: // TODO: Send priest to cult track
				System.out.println("Send priest to cult track");
				sendPriestToCultBoard(cultType, priestPosition);
				// sendPriestToCultBoard(cultType, priestPosition);
				break;
			case 5: // TODO: Power Actions
				System.out.println("Power Actions");
				Random random = new Random();
				int randomActionId = random.nextInt(6);
				powerAction(randomActionId);

				break;
			case 6: // TODO: Special Actions
				System.out.println("SPECIAL ACTION");
				break;
			case 7:
				System.out.println("PASS");
				pass();
				break;
			case 8: // build structure. will be used in the setup phase where each player places
					// 1/2/3 dwellings.
				buildSetupDwelling();
				break;
			case 9:
				// Test for each cult type
				moveOnCultBoard(currentPlayer, Cult.AIR, 2);
				break;
			case 10:
				//t = TerrainType.TERRAINS_INDEXED[terrainTypeIndex];
				performableActions[0] = canTerraformTerrain();
				break;
			case 11:
				performableActions[1] = canBuildDwelling(terrainXPosition, terrainYPosition);
				break;
			case 12:
				performableActions[2] = canUpgradeShipping();
				break;
			case 13:
				performableActions[3] = canUpgradeSpades();
				break;
			case 14:
				performableActions[4] = canUpgradeStructure(terrainXPosition, terrainYPosition);
				break;
			case 15:
				performableActions[5] = canSendPriestToCultBoard(cultType);
				break;
			case 16:
				performableActions[6] = canPerformSpeacialAction();
				break;
			case 17:
				performableActions[7] = canPerformPowerAction(0);
				break;
			case 99: // returns the boolean array indicating if an action is possible
				showPlayAbleActions();
				break;
		}
	}

	public void setTargetTerrainType(TerrainType targetTerrainType) {
		this.targetTerrainType = targetTerrainType;
	}

	public void setTerrainToBeModified(Terrain terrainToBeModified) {
		this.terrainToBeModified = terrainToBeModified;
	}
}