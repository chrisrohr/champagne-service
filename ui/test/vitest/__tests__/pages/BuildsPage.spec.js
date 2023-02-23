import { installQuasar } from '@quasar/quasar-app-extension-testing-unit-vitest'
import { shallowMount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import BuildsPage from 'pages/BuildsPage.vue'
import { createTestingPinia } from '@pinia/testing'

installQuasar()

const wrapper = shallowMount(BuildsPage, {
  global: {
    plugins: [createTestingPinia()]
  }
})

describe('BuildsPage', () => {
  it('should mount the builds page without errors', () => {
    expect(wrapper).toBeTruthy()
  })
})

describe('startPromotionOfBuild', () => {
  it('should alert the text "Coming Soon"', () => {
    alert = vi.fn()

    wrapper.vm.startPromotionOfBuild()

    expect(alert.mock.calls.length).toBe(1)
  })
})

describe('startPreviewPromotion', () => {
  it('should alert the text "Coming Soon"', () => {
    alert = vi.fn()

    wrapper.vm.startPreviewPromotion()

    expect(alert.mock.calls.length).toBe(1)
  })
})

describe('urlForCommitRef', () => {
  it('should generate GitHub url for GitHub', () => {
    const build = {
      repoNamespace: 'kiwiproject',
      repoName: 'champagne-service',
      commitRef: '12345678',
      gitProvider: 'GITHUB'
    }

    const url = wrapper.vm.urlForCommitRef(build)

    expect(url).toContain('https://github.com')
  })

  it('should generate GitLab url for GitLab', () => {
    const build = {
      repoNamespace: 'kiwiproject',
      repoName: 'champagne-service',
      commitRef: '12345678',
      gitProvider: 'GITLAB'
    }

    const url = wrapper.vm.urlForCommitRef(build)

    expect(url).toContain('https://gitlab.com')
  })

  it('should generate Bitbucket url for Bitbucket', () => {
    const build = {
      repoNamespace: 'kiwiproject',
      repoName: 'champagne-service',
      commitRef: '12345678',
      gitProvider: 'BITBUCKET'
    }

    const url = wrapper.vm.urlForCommitRef(build)

    expect(url).toContain('https://bitbucket.org')
  })

  it('should return empty for OTHER', () => {
    const build = {
      repoNamespace: 'kiwiproject',
      repoName: 'champagne-service',
      commitRef: '12345678',
      gitProvider: 'OTHER'
    }

    const url = wrapper.vm.urlForCommitRef(build)

    expect(url).toEqual('')
  })
})

describe('iconForProvider', () => {
  it('should return GitHub icon for GitHub', () => {
    const build = {
      repoNamespace: 'kiwiproject',
      repoName: 'champagne-service',
      commitRef: '12345678',
      gitProvider: 'GITHUB'
    }

    const icon = wrapper.vm.iconForProvider(build)

    expect(icon).toEqual('fa-brands fa-github')
  })

  it('should return GitLab icon for GitLab', () => {
    const build = {
      repoNamespace: 'kiwiproject',
      repoName: 'champagne-service',
      commitRef: '12345678',
      gitProvider: 'GITLAB'
    }

    const icon = wrapper.vm.iconForProvider(build)

    expect(icon).toEqual('fa-brands fa-gitlab')
  })

  it('should return Bitbucket icon for Bitbucket', () => {
    const build = {
      repoNamespace: 'kiwiproject',
      repoName: 'champagne-service',
      commitRef: '12345678',
      gitProvider: 'BITBUCKET'
    }

    const icon = wrapper.vm.iconForProvider(build)

    expect(icon).toEqual('fa-brands fa-bitbucket')
  })

  it('should return generate git icon for OTHER', () => {
    const build = {
      repoNamespace: 'kiwiproject',
      repoName: 'champagne-service',
      commitRef: '12345678',
      gitProvider: 'OTHER'
    }

    const icon = wrapper.vm.iconForProvider(build)

    expect(icon).toEqual('fa-brands fa-git-alt')
  })
})

describe('iconColorForProvider', () => {
  it('should return black for GitHub', () => {
    const build = {
      repoNamespace: 'kiwiproject',
      repoName: 'champagne-service',
      commitRef: '12345678',
      gitProvider: 'GITHUB'
    }

    const color = wrapper.vm.iconColorForProvider(build)

    expect(color).toEqual('black')
  })

  it('should return orange for GitLab', () => {
    const build = {
      repoNamespace: 'kiwiproject',
      repoName: 'champagne-service',
      commitRef: '12345678',
      gitProvider: 'GITLAB'
    }

    const color = wrapper.vm.iconColorForProvider(build)

    expect(color).toEqual('orange')
  })

  it('should return Bitbucket icon for Bitbucket', () => {
    const build = {
      repoNamespace: 'kiwiproject',
      repoName: 'champagne-service',
      commitRef: '12345678',
      gitProvider: 'BITBUCKET'
    }

    const color = wrapper.vm.iconColorForProvider(build)

    expect(color).toEqual('blue')
  })

  it('should return generate git icon for OTHER', () => {
    const build = {
      repoNamespace: 'kiwiproject',
      repoName: 'champagne-service',
      commitRef: '12345678',
      gitProvider: 'OTHER'
    }

    const color = wrapper.vm.iconColorForProvider(build)

    expect(color).toEqual('black')
  })
})
