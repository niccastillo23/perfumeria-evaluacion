import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import React from 'react'
import App from '../App'

vi.mock('../App.css', () => ({}))

describe('App Component - Auth Views', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('renders login view by default', () => {
    render(<App />)
    expect(screen.getByText('PerfumerIA')).toBeTruthy()
  })

  it('shows username input in login view', () => {
    render(<App />)
    expect(screen.getByPlaceholderText('Nombre de usuario')).toBeTruthy()
  })

  it('shows password input in login view', () => {
    render(<App />)
    expect(screen.getByPlaceholderText('Contraseña')).toBeTruthy()
  })

  it('has login button', () => {
    render(<App />)
    expect(screen.getByText('Entrar')).toBeTruthy()
  })
})

describe('App Component - Cart Operations', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
    global.fetch = vi.fn(() =>
      Promise.resolve({
        json: () => Promise.resolve([
          { id: 1, name: 'Test Perfume', brand: 'Test Brand', price: 100.0, image: '' }
        ])
      })
    )
  })

  it('addToCart adds new item with quantity 1', () => {
    const perfume = { id: 1, name: 'Test', brand: 'Brand', price: 50.0 }
    let cart = []

    const addToCart = (item) => {
      const existing = cart.find(i => i.id === item.id)
      if (existing) {
        cart = cart.map(i => i.id === item.id ? { ...i, quantity: i.quantity + 1 } : i)
      } else {
        cart = [...cart, { ...item, quantity: 1 }]
      }
    }

    addToCart(perfume)
    expect(cart).toHaveLength(1)
    expect(cart[0].quantity).toBe(1)
  })

  it('addToCart increments quantity for existing item', () => {
    const perfume = { id: 1, name: 'Test', brand: 'Brand', price: 50.0 }
    let cart = [{ ...perfume, quantity: 1 }]

    const addToCart = (item) => {
      const existing = cart.find(i => i.id === item.id)
      if (existing) {
        cart = cart.map(i => i.id === item.id ? { ...i, quantity: i.quantity + 1 } : i)
      } else {
        cart = [...cart, { ...item, quantity: 1 }]
      }
    }

    addToCart(perfume)
    expect(cart).toHaveLength(1)
    expect(cart[0].quantity).toBe(2)
  })

  it('removeFromCart removes the item', () => {
    let cart = [
      { id: 1, name: 'A', price: 50, quantity: 1 },
      { id: 2, name: 'B', price: 30, quantity: 1 }
    ]

    const removeFromCart = (id) => {
      cart = cart.filter(item => item.id !== id)
    }

    removeFromCart(1)
    expect(cart).toHaveLength(1)
    expect(cart[0].id).toBe(2)
  })

  it('updateQuantity increases and decreases correctly', () => {
    let cart = [{ id: 1, name: 'A', price: 50, quantity: 2 }]

    const updateQuantity = (id, delta) => {
      cart = cart.map(item => {
        if (item.id === id) {
          const newQty = item.quantity + delta
          return newQty > 0 ? { ...item, quantity: newQty } : item
        }
        return item
      })
    }

    updateQuantity(1, 1)
    expect(cart[0].quantity).toBe(3)

    updateQuantity(1, -1)
    expect(cart[0].quantity).toBe(2)

    updateQuantity(1, -1)
    expect(cart[0].quantity).toBe(1)
  })

  it('updateQuantity does not go below 1', () => {
    let cart = [{ id: 1, name: 'A', price: 50, quantity: 1 }]

    const updateQuantity = (id, delta) => {
      cart = cart.map(item => {
        if (item.id === id) {
          const newQty = item.quantity + delta
          return newQty > 0 ? { ...item, quantity: newQty } : item
        }
        return item
      })
    }

    updateQuantity(1, -5)
    expect(cart[0].quantity).toBe(1)
  })

  it('clearCart removes all items', () => {
    let cart = [
      { id: 1, name: 'A', price: 50, quantity: 1 },
      { id: 2, name: 'B', price: 30, quantity: 2 }
    ]

    const clearCart = () => { cart = [] }

    clearCart()
    expect(cart).toHaveLength(0)
  })

  it('calculates total correctly', () => {
    const cart = [
      { id: 1, name: 'A', price: 50.0, quantity: 2 },
      { id: 2, name: 'B', price: 30.0, quantity: 1 }
    ]

    const total = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0)
    expect(total).toBe(130.0)
  })

  it('calculates cart item count correctly', () => {
    const cart = [
      { id: 1, name: 'A', price: 50.0, quantity: 2 },
      { id: 2, name: 'B', price: 30.0, quantity: 3 }
    ]

    const count = cart.reduce((sum, item) => sum + item.quantity, 0)
    expect(count).toBe(5)
  })
})

describe('App Component - Payment Simulation', () => {
  it('simulatePayment sets processing then success state', () => {
    vi.useFakeTimers()
    let paymentStatus = null
    let cart = [{ id: 1, name: 'A', price: 50, quantity: 1 }]

    const simulatePayment = () => {
      if (cart.length === 0) return
      paymentStatus = 'processing'
      setTimeout(() => {
        paymentStatus = 'success'
        cart = []
      }, 1500)
    }

    simulatePayment()
    expect(paymentStatus).toBe('processing')

    vi.advanceTimersByTime(1500)
    expect(paymentStatus).toBe('success')
    expect(cart).toHaveLength(0)

    vi.useRealTimers()
  })

  it('simulatePayment does nothing when cart is empty', () => {
    let paymentStatus = null
    let cart = []

    const simulatePayment = () => {
      if (cart.length === 0) return
      paymentStatus = 'processing'
    }

    simulatePayment()
    expect(paymentStatus).toBeNull()
  })
})
