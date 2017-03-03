/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package org.bbmri.podium.common;

import java.util.UUID;

/**
 * Interface to mark objects associated with a user.
 */
public interface IdentifiableUser {

    UUID getUserUuid();

}
