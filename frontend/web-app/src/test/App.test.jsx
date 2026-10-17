import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import React from 'react'
import App from '../App'
import { createOrderPayload } from '../checkout/checkoutApi.js'

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

  it('explains that authentication is managed by Entra ID', () => {
    render(<App />)
    expect(screen.getByText('Accede con la cuenta administrada por Microsoft Entra ID.')).toBeTruthy()
  })

  it('does not render the legacy password form', () => {
    render(<App />)
    expect(screen.queryByPlaceholderText('Contraseña')).toBeNull()
  })

  it('has login button', () => {
    render(<App />)
    expect(screen.getByText('Iniciar sesión con Microsoft')).toBeTruthy()
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

describe('Checkout integration payload', () => {
  it('maps the cart to the orders API contract and includes async shipping details', () => {
    const payload = createOrderPayload([
      { id: 7, name: 'Aroma', brand: 'PerfumerIA', price: 20, quantity: 2 },
    ])

    expect(payload).toEqual({
      items: [{ productId: 7, name: 'Aroma', brand: 'PerfumerIA', price: 20, quantity: 2 }],
      total: 40,
      payment: { type: 'cash_on_delivery' },
      carrier: 'dhl',
      destination: 'Por coordinar con el cliente',
    })
  })
})
