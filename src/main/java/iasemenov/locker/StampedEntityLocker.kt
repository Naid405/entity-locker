// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package iasemenov.locker

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.StampedLock
import kotlin.concurrent.withLock

class StampedEntityLocker<T> {

  private val entityLocksMapLock = StampedLock()
  //guarded by [entityLocksMapLock]
  private val locksByEntity = ConcurrentHashMap<T, Lock>()

  fun <R> executeUnderEntityLock(entity: T, operation: () -> R): R {
    var readLockStamp = entityLocksMapLock.readLock()
    val (remoteLock, newReadLockStamp) = getEntityLock(readLockStamp, entity)
    readLockStamp = newReadLockStamp
    try {
      val result = remoteLock.withLock(operation)
      readLockStamp = tryRemoveEntityLock(readLockStamp, entity)
      return result
    }
    finally {
      entityLocksMapLock.unlockRead(readLockStamp)
    }
  }

  private fun getEntityLock(readLockStamp: Long, coordinates: T): Pair<Lock, Long> {
    val entityLock = locksByEntity[coordinates]
    if (entityLock != null) return entityLock to readLockStamp

    val writeLockStamp = entityLocksMapLock.tryConvertToWriteLock(readLockStamp)
    if (writeLockStamp == 0L) {
      Thread.yield()
      return getEntityLock(readLockStamp, coordinates)
    }
    else {
      val newEntityLock = ReentrantLock(true)
      locksByEntity[coordinates] = newEntityLock
      return newEntityLock to entityLocksMapLock.tryConvertToReadLock(writeLockStamp)
    }
  }

  private fun tryRemoveEntityLock(readLockStamp: Long, coordinates: T): Long {
    val writeLockStamp = entityLocksMapLock.tryConvertToWriteLock(readLockStamp)

    if (writeLockStamp != 0L) {
      try {
        locksByEntity.remove(coordinates)
      }
      finally {
        return entityLocksMapLock.tryConvertToReadLock(writeLockStamp)
      }
    }
    return readLockStamp
  }
}