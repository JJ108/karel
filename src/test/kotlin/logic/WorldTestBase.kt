package logic

import common.Stack
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import vm.VirtualMachine

import java.util.concurrent.atomic.AtomicReference

open class WorldTestBase : VirtualMachine.Callbacks {
    protected var initialWorld: World = Problem.emptyWorld
    protected var world: World = Problem.emptyWorld

    protected fun executeGoal(problem: Problem) {
        val instructions = vm.createInstructionBuffer()
        instructions.addAll(problem.goal.map { vm.goalInstruction(it.code) })
        initialWorld = problem.randomWorld()
        val atomicWorld = AtomicReference(initialWorld)
        val virtualMachine = VirtualMachine(instructions, atomicWorld, this)
        try {
            virtualMachine.stepReturn()
        } catch (_: Stack.Exhausted) {
        }
        world = atomicWorld.get()
    }

    override fun onInfiniteLoop() {
        fail("infinite loop detected")
    }

    protected fun assertKarelAt(x: Int, y: Int, direction: Int) {
        assertEquals(x, world.x)
        assertEquals(y, world.y)
        assertEquals(direction, world.direction)
    }

    protected fun assertSoleBeeperAt(x: Int, y: Int) {
        val expected = Problem.emptyWorld.dropBeeper(x, y)
        assertEquals(expected.beepersHi, world.beepersHi)
        assertEquals(expected.beepersLo, world.beepersLo)
    }

    protected fun assertSoleBeeperAtKarel() {
        assertSoleBeeperAt(world.x, world.y)
    }

    protected fun assertNoBeepers() {
        assertEquals(0, world.beepersHi)
        assertEquals(0, world.beepersLo)
    }

    protected fun assertNumberOfBeepers(expected: Int) {
        val actual = world.countBeepers()
        assertEquals(expected, actual)
    }

    protected fun assertAllBeepersTouch(walls: Int) {
        for (y in 0 until Problem.HEIGHT) {
            for (x in 0 until Problem.WIDTH) {
                if (world.beeperAt(x, y)) {
                    assertEquals(walls, world.floorPlan.wallsAt(x, y).and(walls))
                }
            }
        }
    }

    protected fun assertNoBeepersTouch(walls: Int) {
        for (y in 0 until Problem.HEIGHT) {
            for (x in 0 until Problem.WIDTH) {
                if (world.beeperAt(x, y)) {
                    assertEquals(FloorPlan.WALL_NONE, world.floorPlan.wallsAt(x, y).and(walls))
                }
            }
        }
    }
}
