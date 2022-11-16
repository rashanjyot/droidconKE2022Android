/*
 * Copyright 2022 DroidconKE
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
package com.android254.data.repos

import com.android254.data.dao.OrganizersDao
import com.android254.data.network.apis.OrganizersApi
import com.android254.data.repos.mappers.toDomain
import com.android254.data.repos.mappers.toEntity
import com.android254.domain.models.DataResult
import com.android254.domain.models.Organizer
import com.android254.domain.models.ResourceResult
import com.android254.domain.repos.OrganizersRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class OrganizersSource @Inject constructor(
    private val api: OrganizersApi,
    private val dao: OrganizersDao
) : OrganizersRepo {

    override suspend fun fetchOrganizers(): ResourceResult<List<Organizer>> = withContext(Dispatchers.IO) {
        val dbObjs = dao.fetchOrganizers()
        if (dbObjs.isEmpty()) {
            withContext(Dispatchers.Default) {
                val result = api.fetchOrganizers("individual")
                if (result is DataResult.Success) {
                    val data = result.data
                    dao.insert(data.data.map { it.toEntity() })
                }
            }
            withContext(Dispatchers.Default) {
                val result = api.fetchOrganizers("company")
                if (result is DataResult.Success) {
                    val data = result.data
                    dao.insert(data.data.map { it.toEntity() })
                }
            }
        }
        return@withContext ResourceResult.Success(dao.fetchOrganizers().map { it.toDomain() })
    }
}