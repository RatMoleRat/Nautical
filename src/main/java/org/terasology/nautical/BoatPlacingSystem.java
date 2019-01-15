/*
 * Copyright 2018 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.nautical;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.math.ChunkMath;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.network.NetworkSystem;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.items.BlockItemComponent;

@RegisterSystem(RegisterMode.AUTHORITY)
public class BoatPlacingSystem extends BaseComponentSystem {
    private static final float ADDITIONAL_ALLOWED_PENETRATION = 0.4f;

    private static Logger logger = LoggerFactory.getLogger(BoatPlacingSystem.class);

    @In
    private NetworkSystem networkSystem;

    @In
    private WorldProvider worldProvider;

    @In
    private BlockEntityRegistry blockEntityRegistry;

    @In
    private BoatMovingSystem boatMovingSystem;

    @In
    private EntityManager entityManager;

    public void initialise() {
        //TODO: get a better solution than this, as now things underwater can't be mined
        CoreRegistry.get(BlockManager.class).getBlock("Core:Water").setTargetable(true);
    }

    @ReceiveEvent(components = {BlockItemComponent.class, ItemComponent.class}, priority = EventPriority.PRIORITY_CRITICAL)
    public void onPlaceBlock(ActivateEvent event, EntityRef item) {
        if (!event.getTarget().exists()) {
            event.consume();
            return;
        }
        logger.info("event triggered");

        BlockItemComponent blockItem = item.getComponent(BlockItemComponent.class);
        BlockFamily type = blockItem.blockFamily;
        Side surfaceSide = Side.inDirection(event.getHitNormal());
        Side secondaryDirection = ChunkMath.getSecondaryPlacementDirection(event.getDirection(), event.getHitNormal());

        BlockComponent blockComponent = event.getTarget().getComponent(BlockComponent.class);
        if (blockComponent != null) {
            Vector3i targetBlock = new Vector3i(blockComponent.position);
            Vector3i placementPos = new Vector3i(targetBlock);
            placementPos.add(surfaceSide.getVector3i());

            Block block = type.getBlockForPlacement(placementPos, surfaceSide, secondaryDirection);

            logger.info("block id: " + block.getDisplayName());
            if (block.getDisplayName().equals("Boat")) {
                if (canPlaceBlock(block, targetBlock, placementPos)) {
                    logger.info("can place");
                    if (networkSystem.getMode().isAuthority()) {
                        logger.info("placing boat");
                        boatMovingSystem.boat(entityManager.create("Boat", new Vector3f(placementPos.x, placementPos.y, placementPos.z)));
                    }
                }
            }
        }
    }

    private boolean canPlaceBlock(Block block, Vector3i targetBlock, Vector3i blockPos) {
        if (block == null) {
            return false;
        }

        Block centerBlock = worldProvider.getBlock(targetBlock.x, targetBlock.y, targetBlock.z);
        logger.info(centerBlock.getDisplayName());
        if (!centerBlock.equals(CoreRegistry.get(BlockManager.class).getBlock("Core:Water"))) {
            return false;
        }
        logger.info("is water");
        Block adjBlock = worldProvider.getBlock(blockPos.x, blockPos.y+1, blockPos.z);
        logger.info(adjBlock.getDisplayName());
        if (!adjBlock.equals(CoreRegistry.get(BlockManager.class).getBlock("engine:air"))) {
            return false;
        }
        logger.info("adjacent is water");
        if (block.getBlockFamily().equals(adjBlock.getBlockFamily())) {
            return false;
        }

        /*
        // Prevent players from placing blocks inside their bounding boxes
        if (!block.isPenetrable()) {
            Physics physics = CoreRegistry.get(Physics.class);
            AABB blockBounds = block.getBounds(blockPos);
            Vector3f min = new Vector3f(blockBounds.getMin());
            Vector3f max = new Vector3f(blockBounds.getMax());

            /**
             * Characters can enter other solid objects/blocks for certain amount. This is does to detect collsion
             * start and end without noise. So if the user walked as close to a block as possible it is only natural
             * to let it place a block exactly above it even if that technically would mean a collision start.
             */
        /*
            min.x += KinematicCharacterMover.HORIZONTAL_PENETRATION;
            max.x -= KinematicCharacterMover.HORIZONTAL_PENETRATION;
            min.y += KinematicCharacterMover.VERTICAL_PENETRATION;
            max.y -= KinematicCharacterMover.VERTICAL_PENETRATION;
            min.z += KinematicCharacterMover.HORIZONTAL_PENETRATION;
            max.z -= KinematicCharacterMover.HORIZONTAL_PENETRATION;

            /*
             * Calculations aren't exact and in the corner cases it is better to let the user place the block.
             */
        /*
            float additionalAllowedPenetration = 0.04f; // ignore small rounding mistakes
            min.add(ADDITIONAL_ALLOWED_PENETRATION, ADDITIONAL_ALLOWED_PENETRATION, ADDITIONAL_ALLOWED_PENETRATION);
            max.sub(ADDITIONAL_ALLOWED_PENETRATION, ADDITIONAL_ALLOWED_PENETRATION, ADDITIONAL_ALLOWED_PENETRATION);

            AABB newBounds = AABB.createMinMax(min, max);
            return physics.scanArea(newBounds, StandardCollisionGroup.DEFAULT, StandardCollisionGroup.CHARACTER).isEmpty();
        }*/
        return true;
    }
}
