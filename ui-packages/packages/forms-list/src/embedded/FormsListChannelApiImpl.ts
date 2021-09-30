/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {
  FormsListDriver,
  FormsListChannelApi,
  FormFilter,
  FormInfo
} from '../api';

/**
 * Implementation of the FormsListChannelApiImpl delegating to a FormsListDriver
 */
export class FormsListChannelApiImpl implements FormsListChannelApi {
  constructor(private readonly driver: FormsListDriver) {}

  formsList__getFormFilter(): Promise<FormFilter> {
    return this.driver.getFormFilter();
  }

  formsList__applyFilter(formFilter: FormFilter): Promise<void> {
    return this.driver.applyFilter(formFilter);
  }

  formsList__getFormsQuery(): Promise<FormInfo[]> {
    return this.driver.getFormsQuery();
  }

  formsList__openForm(formData: FormInfo): Promise<void> {
    return this.driver.openForm(formData);
  }
}