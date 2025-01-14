import * as React from 'react';
import { mount } from 'enzyme';
import { MemoryRouter } from 'react-router';
import TrustyApp from '../TrustyApp';
import { RemoteDataStatus } from '../../../../types';
import useExecutions from '../../AuditOverview/useExecutions';

jest.mock('../../AuditOverview/useExecutions');
const mockLoadExecutions = jest.fn();

describe('TrustyApp', () => {
  test('does not include the Page template when pageWrapper prop is false', () => {
    const wrapper = mount(
      <MemoryRouter
        initialEntries={[
          {
            pathname: '/some-non-existent-page',
            key: 'some-none-existent-page'
          }
        ]}
      >
        <TrustyApp
          explanationEnabled={true}
          counterfactualEnabled={true}
          containerConfiguration={{
            excludeReactRouter: true,
            pageWrapper: false
          }}
        />
      </MemoryRouter>
    );

    expect(wrapper.find('Page')).toHaveLength(0);
  });

  test('includes the Page template when pageWrapper is true', () => {
    const wrapper = mount(
      <MemoryRouter
        initialEntries={[
          {
            pathname: '/some-non-existent-page',
            key: 'some-none-existent-page'
          }
        ]}
      >
        <TrustyApp
          explanationEnabled={true}
          counterfactualEnabled={true}
          containerConfiguration={{
            excludeReactRouter: true,
            pageWrapper: true
          }}
        />
      </MemoryRouter>
    );

    expect(wrapper.find('Page')).toHaveLength(1);
  });

  test('adds the provided base path to links', () => {
    const wrapper = mount(
      <MemoryRouter
        initialEntries={[
          {
            pathname: '/some-non-existent-page',
            key: 'some-none-existent-page'
          }
        ]}
      >
        <TrustyApp
          explanationEnabled={true}
          counterfactualEnabled={true}
          containerConfiguration={{
            excludeReactRouter: true,
            pageWrapper: true,
            basePath: '/trusty-base-path'
          }}
        />
      </MemoryRouter>
    );

    expect(
      wrapper
        .find('a.trusty-home-link')
        .at(0)
        .props().href
    ).toMatch('/trusty-base-path/');
  });

  test('uses "/" as base path for links when no basePath prop is provided', () => {
    const wrapper = mount(
      <MemoryRouter
        initialEntries={[
          {
            pathname: '/some-non-existent-page',
            key: 'some-none-existent-page'
          }
        ]}
      >
        <TrustyApp
          explanationEnabled={true}
          counterfactualEnabled={true}
          containerConfiguration={{
            excludeReactRouter: true,
            pageWrapper: true
          }}
        />
      </MemoryRouter>
    );

    expect(
      wrapper
        .find('a.trusty-home-link')
        .at(0)
        .props().href
    ).toMatch('/');
  });

  test('does not include a react router when excludeReactRouter is set to true', () => {
    const wrapper = mount(
      <MemoryRouter
        initialEntries={[
          {
            pathname: '/some-non-existent-page',
            key: 'some-none-existent-page'
          }
        ]}
      >
        <TrustyApp
          explanationEnabled={true}
          counterfactualEnabled={true}
          containerConfiguration={{
            excludeReactRouter: true
          }}
        />
      </MemoryRouter>
    );

    expect(wrapper.find('BrowserRouter')).toHaveLength(0);
  });

  test('adds a react BrowserRouter when excludeReactRouter is set to false', () => {
    (useExecutions as jest.Mock).mockReturnValue({
      mockLoadExecutions,
      executions
    });
    const wrapper = mount(
      <TrustyApp
        explanationEnabled={true}
        counterfactualEnabled={true}
        containerConfiguration={{
          excludeReactRouter: false
        }}
      />
    );

    expect(wrapper.find('BrowserRouter')).toHaveLength(1);
  });

  test('renders the not found page when a path is not matched', () => {
    const wrapper = mount(
      <MemoryRouter
        initialEntries={[
          {
            pathname: '/some-non-existent-page',
            key: 'some-none-existent-page'
          }
        ]}
      >
        <TrustyApp
          explanationEnabled={true}
          counterfactualEnabled={true}
          containerConfiguration={{
            excludeReactRouter: true
          }}
        />
      </MemoryRouter>
    );

    expect(wrapper.find('NotFound')).toHaveLength(1);
  });
});

const executions = {
  status: RemoteDataStatus.SUCCESS,
  data: {
    total: 1,
    limit: 10,
    offset: 0,
    headers: [
      {
        executionId: 'b2b0ed8d-c1e2-46b5-ad4f-3ac54ff4beae',
        executionDate: '2020-06-01T12:33:57+0000',
        executionSucceeded: true,
        executorName: 'testUser',
        executedModelName: 'LoanEligibility',
        executionType: 'DECISION'
      }
    ]
  }
};
