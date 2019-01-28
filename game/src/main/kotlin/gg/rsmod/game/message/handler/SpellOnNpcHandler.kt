package gg.rsmod.game.message.handler

import gg.rsmod.game.message.MessageHandler
import gg.rsmod.game.message.impl.SpellOnNpcMessage
import gg.rsmod.game.model.INTERACTING_COMPONENT_CHILD
import gg.rsmod.game.model.INTERACTING_COMPONENT_PARENT
import gg.rsmod.game.model.INTERACTING_NPC_ATTR
import gg.rsmod.game.model.Privilege
import gg.rsmod.game.model.entity.Client
import gg.rsmod.game.model.entity.Entity

/**
 * @author Tom <rspsmods@gmail.com>
 */
class SpellOnNpcHandler : MessageHandler<SpellOnNpcMessage> {

    override fun handle(client: Client, message: SpellOnNpcMessage) {
        val npc = client.world.npcs.get(message.npcIndex) ?: return
        val parent = message.componentHash shr 16
        val child = message.componentHash and 0xFFFF

        log(client, "Spell on npc: npc=%d. index=%d, component=[%d, %d], movement=%d", npc.id, message.npcIndex, parent, child, message.movementType)

        client.interruptPlugins()
        client.resetInteractions()

        if (message.movementType == 1 && client.world.privileges.isEligible(client.privilege, Privilege.ADMIN_POWER)) {
            client.teleport(client.world.findRandomTileAround(npc.tile, 1) ?: npc.tile)
        }

        client.attr[INTERACTING_NPC_ATTR] = npc
        client.attr[INTERACTING_COMPONENT_PARENT] = parent
        client.attr[INTERACTING_COMPONENT_CHILD] = child

        if (!client.world.plugins.executeSpellOnNpc(client, parent, child)) {
            client.message(Entity.NOTHING_INTERESTING_HAPPENS)
            if (client.world.devContext.debugMagicSpells) {
                client.message("Unhandled magic spell: [$parent, $child]")
            }
        }
    }
}