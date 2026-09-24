import React, { useState, useEffect } from 'react';
import { useIsAuthenticated, useMsal } from '@azure/msal-react';

import { accountToUser, isEntraConfigured, loginRequest, protectedResources } from './auth/authConfig.js';
import { apiFetch } from './auth/apiClient.js';
import './App.css';

function getProductImage(perfume) {
  const id = perfume?.id || 0;
  const images = [
    'https://images.unsplash.com/photo-1541643600914-78b084683601?w=300&h=400&fit=crop&crop=center',
    'https://images.unsplash.com/photo-1592945403244-b3fbafd7f539?w=300&h=400&fit=crop&crop=center',
    'https://images.unsplash.com/photo-1615634260167-c8cdede054de?w=300&h=400&fit=crop&crop=center',
    'https://images.unsplash.com/photo-1594035910387-fea47794261f?w=300&h=400&fit=crop&crop=center',
    'https://images.unsplash.com/photo-1622618991746-fe6004db3a47?w=300&h=400&fit=crop&crop=center',
    'https://images.unsplash.com/photo-1612817288484-6f916006741a?w=300&h=400&fit=crop&crop=center',
    'https://images.unsplash.com/photo-1595425970377-c9703cf48b6d?w=300&h=400&fit=crop&crop=center',
    'https://images.unsplash.com/photo-1618331833071-ce81bd50d300?w=300&h=400&fit=crop&crop=center',
    'https://images.unsplash.com/photo-1608528577891-eb055944f2e7?w=300&h=400&fit=crop&crop=center',
    'https://images.unsplash.com/photo-1616949755610-8c9bbc08f138?w=300&h=400&fit=crop&crop=center',
    'https://images.unsplash.com/photo-1611078489935-0cb964de46d6?w=300&h=400&fit=crop&crop=center',
    'https://images.unsplash.com/photo-1595341888016-a392ef81b7de?w=300&h=400&fit=crop&crop=center',
    'https://images.unsplash.com/photo-1618331833071-ce81bd50d300?w=300&h=400&fit=crop&crop=center',
    'https://images.unsplash.com/photo-1595425970377-c9703cf48b6d?w=300&h=400&fit=crop&crop=center',
    'https://images.unsplash.com/photo-1601004890684-d8cbf643f5f2?w=300&h=400&fit=crop&crop=center',
    'https://images.unsplash.com/photo-1523293182086-7651a899d37f?w=300&h=400&fit=crop&crop=center',
    'https://images.unsplash.com/photo-1556228453-efd6c1ff04f6?w=300&h=400&fit=crop&crop=center',
    'https://images.unsplash.com/photo-1601004890684-d8cbf643f5f2?w=300&h=400&fit=crop&crop=center',
    'https://images.unsplash.com/photo-1560343090-f0409e92791a?w=300&h=400&fit=crop&crop=center',
    'https://images.unsplash.com/photo-1595341888016-a392ef81b7de?w=300&h=400&fit=crop&crop=center',
    'https://images.unsplash.com/photo-1556228453-efd6c1ff04f6?w=300&h=400&fit=crop&crop=center',
    'https://images.unsplash.com/photo-1560343090-f0409e92791a?w=300&h=400&fit=crop&crop=center',
    'https://images.unsplash.com/photo-1585386959984-a4155224a1ad?w=300&h=400&fit=crop&crop=center',
    'https://images.unsplash.com/photo-1598300042247-d088f8ab3a91?w=300&h=400&fit=crop&crop=center',
    'https://images.unsplash.com/photo-1594035910387-fea47794261f?w=300&h=400&fit=crop&crop=center',
    'https://images.unsplash.com/photo-1541961017774-22349e4a1262?w=300&h=400&fit=crop&crop=center',
    'https://images.unsplash.com/photo-1585386959984-a4155224a1ad?w=300&h=400&fit=crop&crop=center',
    'https://images.unsplash.com/photo-1572635196237-14b3f281503f?w=300&h=400&fit=crop&crop=center',
    'https://images.unsplash.com/photo-1598300042247-d088f8ab3a91?w=300&h=400&fit=crop&crop=center',
    'https://images.unsplash.com/photo-1572635196237-14b3f281503f?w=300&h=400&fit=crop&crop=center'
  ];
  
  const index = (id - 1) % images.length;
  return perfume?.image || images[index];
}

function App() {
  const { instance, accounts, inProgress } = useMsal();
  const isAuthenticated = useIsAuthenticated();
  const account = accounts[0] ?? null;
  const [perfumes, setPerfumes] = useState([]);

  const [cart, setCart] = useState([]);
  const [paymentStatus, setPaymentStatus] = useState(null);
  const [currentView, setCurrentView] = useState('login');
  const [user, setUser] = useState(null);
  const [profileData, setProfileData] = useState(null);
  const [adminData, setAdminData] = useState(null);
  const [adminLoading, setAdminLoading] = useState(false);
  const [usingMockPerfumes, setUsingMockPerfumes] = useState(false);
  const [usingMockAdminData, setUsingMockAdminData] = useState(false);
  const [authMessage, setAuthMessage] = useState({ text: '', type: '' });
  const [showRegister, setShowRegister] = useState(false);
  const [registerForm, setRegisterForm] = useState({ displayName: '', email: '', password: '' });
  const [registerMessage, setRegisterMessage] = useState({ text: '', type: '' });
  const [registerLoading, setRegisterLoading] = useState(false);

  const canManageAdmin = user?.role === 'ADMIN' || user?.role === 'EXECUTIVE';

  useEffect(() => {
    if (isAuthenticated && account) {
      setUser(accountToUser(account));
      setCurrentView('catalog');
      return;
    }

    if (!isAuthenticated && inProgress === 'none') {
      setUser(null);
      setCurrentView('login');
    }
  }, [account, inProgress, isAuthenticated]);

  useEffect(() => {
    if (!isAuthenticated || !account) {
      return undefined;
    }

    let cancelled = false;

    const loadCatalog = async (attempt = 0) => {
      try {
        const response = await apiFetch({
          instance,
          account,
          path: '/api/v1/shop/catalog',
        });

        if (!response.ok) {
          throw new Error(`La API respondió con ${response.status}`);
        }

        const data = await response.json();

        if (!Array.isArray(data)) {
          throw new Error('Invalid data');
        }

        if (cancelled) return;
        setPerfumes(data);
        setUsingMockPerfumes(false);
      } catch (error) {
        if (cancelled) return;

        if (attempt === 0) {
              console.error("Error fetching catalog, using mock data:", error);
              setPerfumes([
                { id: 1, name: "Elegance Gold", brand: "Versace", price: 189.99, image: "https://images.unsplash.com/photo-1541643600914-78b084683601?w=300&h=400&fit=crop&crop=center" },
                { id: 2, name: "Ocean Breeze", brand: "Nautica", price: 125.50, image: "https://images.unsplash.com/photo-1592945403244-b3fbafd7f539?w=300&h=400&fit=crop&crop=center" },
                { id: 3, name: "La Belle", brand: "Jean Paul Gaultier", price: 210.00, image: "https://images.unsplash.com/photo-1615634260167-c8cdede054de?w=300&h=400&fit=crop&crop=center" },
                { id: 4, name: "Wooden Sage", brand: "Terra Nova", price: 145.75, image: "https://images.unsplash.com/photo-1594035910387-fea47794261f?w=300&h=400&fit=crop&crop=center" },
                { id: 5, name: "Golden Amber", brand: "Chanel", price: 275.00, image: "https://images.unsplash.com/photo-1622618991746-fe6004db3a47?w=300&h=400&fit=crop&crop=center" },
                { id: 6, name: "Mystic Oud", brand: "Orient Express", price: 320.50, image: "https://images.unsplash.com/photo-1612817288484-6f916006741a?w=300&h=400&fit=crop&crop=center" },
                { id: 7, name: "Aqua Di Gio", brand: "Giorgio Armani", price: 165.00, image: "https://images.unsplash.com/photo-1595425970377-c9703cf48b6d?w=300&h=400&fit=crop&crop=center" },
                { id: 8, name: "Savage", brand: "Dior", price: 220.00, image: "https://images.unsplash.com/photo-1618331833071-ce81bd50d300?w=300&h=400&fit=crop&crop=center" },
                { id: 9, name: "Bleu", brand: "Chanel", price: 280.00, image: "https://images.unsplash.com/photo-1608528577891-eb055944f2e7?w=300&h=400&fit=crop&crop=center" },
                { id: 10, name: "Eros", brand: "Versace", price: 195.00, image: "https://images.unsplash.com/photo-1616949755610-8c9bbc08f138?w=300&h=400&fit=crop&crop=center" },
                { id: 11, name: "One Million", brand: "Paco Rabanne", price: 170.00, image: "https://images.unsplash.com/photo-1611078489935-0cb964de46d6?w=300&h=400&fit=crop&crop=center" }
              ]);
              setUsingMockPerfumes(true);
            }

        if (attempt < 10) {
          setTimeout(() => loadCatalog(attempt + 1), 1000);
        }
      }
    };

    loadCatalog();

    return () => {
      cancelled = true;
    };
  }, [account, instance, isAuthenticated]);

  const handleLogout = async () => {
    setUser(null);
    setProfileData(null);
    setAdminData(null);
    setAuthMessage({ text: '', type: '' });
    setCurrentView('login');

    if (isAuthenticated) {
      await instance.logoutRedirect({
        account,
        postLogoutRedirectUri: window.location.origin,
      });
    }
  };

  const handleMsalLogin = async () => {
    setAuthMessage({ text: '', type: '' });

    if (!isEntraConfigured) {
      setAuthMessage({
        text: 'Microsoft Entra ID todavía no está configurado. Define las variables VITE_AZURE_CLIENT_ID, VITE_AZURE_TENANT_ID y VITE_API_SCOPE.',
        type: 'error',
      });
      return;
    }

    try {
      await instance.loginRedirect(loginRequest);
    } catch (error) {
      setAuthMessage({ text: error.message, type: 'error' });
    }
  };

  const handleRegister = async (event) => {
    event.preventDefault();
    setRegisterMessage({ text: '', type: '' });
    setRegisterLoading(true);

    try {
      const response = await fetch(`${protectedResources.api.endpoint}/api/v1/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(registerForm),
      });

      const data = await response.json().catch(() => ({}));

      if (!response.ok) {
        throw new Error(data.error || `La API respondió con ${response.status}`);
      }

      setRegisterMessage({ text: 'Cuenta creada. Ahora inicia sesión con Microsoft.', type: 'success' });
      setRegisterForm({ displayName: '', email: '', password: '' });
    } catch (error) {
      setRegisterMessage({ text: error.message, type: 'error' });
    } finally {
      setRegisterLoading(false);
    }
  };

  const fetchProfile = async () => {
    if (!user) return;
    try {
      const response = await apiFetch({
        instance,
        account,
        path: `/api/v1/profile/${encodeURIComponent(user.username)}`,
      });

      if (!response.ok) {
        throw new Error(`La API respondió con ${response.status}`);
      }

      const data = await response.json();
      setProfileData(data);
      setCurrentView('profile');
    } catch (error) {
      console.error("Error fetching profile:", error);
    }
  };

  const fetchAdminData = async () => {
    if (!canManageAdmin) return;
    setAdminLoading(true);
    setCurrentView('admin');
    try {
      const [statsRes, usersRes] = await Promise.all([
        apiFetch({ instance, account, path: '/api/v1/admin/stats' }),
        apiFetch({ instance, account, path: '/api/v1/admin/users' }),
      ]);

      if (!statsRes.ok || !usersRes.ok) {
        throw new Error(`Admin API respondió ${statsRes.status}/${usersRes.status}`);
      }

      let stats = await statsRes.json();
      let usersData = await usersRes.json();
      
      if (!stats) {
        stats = {
          totalSales: 0,
          totalOrders: 0,
          activeUsers: 0,
          topProduct: 'N/A',
          errors: [],
          hasErrors: true
        };
      }
      
      let usersList = usersData.users || [];
      let usersError = usersData.error || null;
      
      if (!Array.isArray(usersList)) {
        usersList = [];
      }
      
      setAdminData({ stats, users: usersList, usersError });
      setUsingMockAdminData(false);
    } catch (error) {
      console.error("Error fetching admin data:", error);
      setAdminData({ 
        stats: {
          totalSales: 0,
          totalOrders: 0,
          activeUsers: 0,
          topProduct: 'N/A',
          errors: ['No se pudo conectar con admin-service'],
          hasErrors: true
        },
        users: [],
        usersError: 'Error de conexión con admin-service'
      });
      setUsingMockAdminData(false);
    } finally {
      setAdminLoading(false);
    }
  };


  const addToCart = (perfume) => {
    const existingItem = cart.find(item => item.id === perfume.id);
    if (existingItem) {
      setCart(cart.map(item => 
        item.id === perfume.id ? { ...item, quantity: item.quantity + 1 } : item
      ));
    } else {
      setCart([...cart, { ...perfume, quantity: 1 }]);
    }
  };

  const removeFromCart = (productId) => {
    setCart(cart.filter(item => item.id !== productId));
  };

  const updateQuantity = (productId, delta) => {
    setCart(cart.map(item => {
      if (item.id === productId) {
        const newQuantity = item.quantity + delta;
        return newQuantity > 0 ? { ...item, quantity: newQuantity } : item;
      }
      return item;
    }));
  };

  const clearCart = () => {
    setCart([]);
  };

  const simulatePayment = () => {
    if (cart.length === 0) return;
    setPaymentStatus('processing');
    setTimeout(() => {
      setPaymentStatus('success');
      setCart([]);
      setTimeout(() => {
        setPaymentStatus(null);
        setCurrentView('catalog');
      }, 3000);
    }, 1500);
  };

  const total = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
  const cartItemCount = cart.reduce((sum, item) => sum + item.quantity, 0);

  if (currentView === 'login') {
    return (
      <div className="auth-container">
        <div className="auth-card">
          <img src="/logo.png" alt="PerfumerIA Logo" className="auth-logo" />
          <h1>PerfumerIA</h1>
          <h2>Iniciar sesión</h2>

          {authMessage.text && (
            <div className={`auth-alert ${authMessage.type}`} onClick={() => setAuthMessage({ text: '', type: '' })} style={{cursor: 'pointer'}}>
              {authMessage.text}
            </div>
          )}

          <p className="auth-subtitle">
            Accede con la cuenta administrada por Microsoft Entra ID.
          </p>
          <button type="button" className="btn-auth" onClick={handleMsalLogin}>
            Iniciar sesión con Microsoft
          </button>

          <button
            type="button"
            onClick={() => {
              setShowRegister(!showRegister);
              setRegisterMessage({ text: '', type: '' });
            }}
            style={{ marginTop: '14px', background: 'none', border: 'none', color: '#8a6d3b', textDecoration: 'underline', cursor: 'pointer', fontSize: '0.9rem' }}
          >
            {showRegister ? 'Volver a iniciar sesión' : 'Crear cuenta'}
          </button>

          {showRegister && (
            <form onSubmit={handleRegister} style={{ marginTop: '16px', display: 'flex', flexDirection: 'column', gap: '10px' }}>
              {registerMessage.text && (
                <div className={`auth-alert ${registerMessage.type}`}>{registerMessage.text}</div>
              )}
              <input
                type="text"
                placeholder="Nombre completo"
                value={registerForm.displayName}
                onChange={(e) => setRegisterForm({ ...registerForm, displayName: e.target.value })}
                required
                style={{ padding: '10px', border: '1px solid #ccc', borderRadius: '4px' }}
              />
              <input
                type="email"
                placeholder="Correo (nombre@dominio.com)"
                value={registerForm.email}
                onChange={(e) => setRegisterForm({ ...registerForm, email: e.target.value })}
                required
                style={{ padding: '10px', border: '1px solid #ccc', borderRadius: '4px' }}
              />
              <input
                type="password"
                placeholder="Contraseña"
                value={registerForm.password}
                onChange={(e) => setRegisterForm({ ...registerForm, password: e.target.value })}
                required
                style={{ padding: '10px', border: '1px solid #ccc', borderRadius: '4px' }}
              />
              <button type="submit" className="btn-auth" disabled={registerLoading}>
                {registerLoading ? 'Creando cuenta...' : 'Crear cuenta'}
              </button>
            </form>
          )}
        </div>
      </div>
    );
  }

  return (
    <div className="ecommerce-container">
      <header className="header">
        <div className="logo" onClick={() => setCurrentView('catalog')} style={{cursor: 'pointer'}}>
          <img src="/logo.png" alt="PerfumerIA Logo" className="header-logo" />
          <div className="logo-text">
            <h1>PerfumerIA</h1>
            <p>Arte en fragancias</p>
          </div>
        </div>
        <div className="nav-actions">
          {user && (
            <div className="user-menu">
              <span className="user-welcome" onClick={fetchProfile} style={{cursor: 'pointer', textDecoration: 'underline'}}>
                Hola, {user.username} ({user.role})
              </span>
              {canManageAdmin && (
                <button className={`btn-nav ${currentView === 'admin' ? 'active' : ''}`} onClick={fetchAdminData}>Panel Admin</button>
              )}
            </div>
          )}
          <button 
            className={`cart-link ${currentView === 'cart' ? 'active' : ''}`}
            onClick={() => setCurrentView('cart')}
          >
            <span className="cart-icon">🛒</span>
            <span className="cart-text">Carrito</span>
            {cartItemCount > 0 && <span className="cart-count">{cartItemCount}</span>}
          </button>
          <button className="btn-logout" onClick={handleLogout}>Salir</button>
        </div>
      </header>

      {paymentStatus === 'success' && (
        <div className="alert success">
          ¡Pago realizado con éxito! Gracias por su compra en PerfumerIA.
        </div>
      )}

      <main className="main-content">
        {currentView === 'catalog' && (
          <section className="catalog-view">
            <div className="view-header">
              <h2>Catálogo de Fragancias</h2>
              <p>Explora nuestra exclusiva colección de {perfumes.length} aromas premium</p>
              {usingMockPerfumes && (
                <div style={{
                  background: '#fff3cd',
                  border: '1px solid #ffc107',
                  padding: '10px 20px',
                  marginTop: '20px',
                  borderRadius: '4px',
                  fontSize: '0.85rem',
                  textTransform: 'uppercase',
                  letterSpacing: '1px',
                  color: '#856404'
                }}>
                  ⚠️ Usando datos de ejemplo (microservicio no disponible)
                </div>
              )}
            </div>
            <div className="perfume-grid">
              {perfumes.map(perfume => {
                const id = perfume?.id ?? Math.random().toString(36).substr(2, 9);
                const name = perfume?.name ?? 'Producto sin nombre';
                const brand = perfume?.brand ?? 'Marca desconocida';
                const price = perfume?.price ?? 0;
                const image = getProductImage(perfume);
                
                return (
                  <div key={id} className="perfume-card">
                    <div className="image-container">
                      <img src={image} alt={name} onError={(e) => { e.target.onerror = null; e.target.src = getProductImage({ id, name: 'fallback' }); }} />
                    </div>
                    <div className="card-info">
                      <h3>{name}</h3>
                      <p className="brand">{brand}</p>
                      <p className="price">${typeof price === 'number' ? price.toFixed(2) : '0.00'}</p>
                      <button className="btn-add" onClick={() => addToCart({ ...perfume, id, name, brand, price })}>Añadir al Carrito</button>
                    </div>
                  </div>
                );
              })}
            </div>
          </section>
        )}

        {currentView === 'cart' && (
          <section className="cart-view">
            <div className="view-header">
              <button className="btn-back" onClick={() => setCurrentView('catalog')}>← Volver al Catálogo</button>
              <h2>Tu Carrito de Compras</h2>
            </div>

            <div className="cart-container-full">
              {cart.length === 0 ? (
                <div className="empty-cart-state">
                  <span className="big-icon">🛒</span>
                  <p>Tu carrito está actualmente vacío.</p>
                  <button className="btn-primary" onClick={() => setCurrentView('catalog')}>Explorar Productos</button>
                </div>
              ) : (
                <div className="cart-layout">
                  <div className="cart-items-list">
                    <div className="list-header">
                      <span>Producto</span>
                      <span>Cantidad</span>
                      <span>Subtotal</span>
                      <span>Acción</span>
                    </div>
                    {cart.map(item => {
                      const id = item?.id ?? Math.random().toString(36).substr(2, 9);
                      const name = item?.name ?? 'Producto sin nombre';
                      const brand = item?.brand ?? '';
                      const price = typeof item?.price === 'number' ? item.price : 0;
                      const quantity = typeof item?.quantity === 'number' ? item.quantity : 1;
                      const image = getProductImage(item).replace('300&h=400', '70&h=70');
                      
                      return (
                        <div key={id} className="cart-item-row">
                          <div className="product-info">
                            <img src={image} alt={name} onError={(e) => { e.target.onerror = null; e.target.src = getProductImage({ id, name: 'fallback' }).replace('300&h=400', '70&h=70'); }} />
                            <div>
                              <h4>{name}</h4>
                              <p>{brand}</p>
                            </div>
                          </div>
                          <div className="quantity-cell">
                            <div className="quantity-controls">
                              <button onClick={() => updateQuantity(id, -1)}>-</button>
                              <span>{quantity}</span>
                              <button onClick={() => updateQuantity(id, 1)}>+</button>
                            </div>
                          </div>
                          <div className="price-cell">
                            ${(price * quantity).toFixed(2)}
                          </div>
                          <div className="action-cell">
                            <button className="btn-remove-text" onClick={() => removeFromCart(id)}>Eliminar</button>
                          </div>
                        </div>
                      );
                    })}
                    <button className="btn-clear-all" onClick={clearCart}>Vaciar Carrito</button>
                  </div>

                  <div className="cart-checkout-card">
                    <h3>Resumen del Pedido</h3>
                    <div className="summary-details">
                      <div className="summary-line">
                        <span>Productos ({cartItemCount})</span>
                        <span>${total.toFixed(2)}</span>
                      </div>
                      <div className="summary-line">
                        <span>Envío</span>
                        <span className="free">Gratis</span>
                      </div>
                      <div className="summary-line total-line">
                        <span>Total a Pagar</span>
                        <span>${total.toFixed(2)}</span>
                      </div>
                    </div>
                    <button 
                      className={`btn-checkout-full ${paymentStatus === 'processing' ? 'loading' : ''}`}
                      onClick={simulatePayment}
                      disabled={paymentStatus === 'processing'}
                    >
                      {paymentStatus === 'processing' ? 'Procesando Pago...' : 'Confirmar y Pagar'}
                    </button>
                    <p className="secure-text">🔒 Pago 100% seguro</p>
                  </div>
                </div>
              )}
            </div>
          </section>
        )}

        {currentView === 'profile' && profileData && (
          <section className="profile-view">
            <div className="view-header">
              <button className="btn-back" onClick={() => setCurrentView('catalog')}>← Volver al Catálogo</button>
              <h2>Mi Perfil</h2>
            </div>
            <div className="profile-card">
              <div className="profile-header">
                <div className="profile-avatar">{user.username.charAt(0).toUpperCase()}</div>
                <div className="profile-info-main">
                  <h3>{profileData.fullName}</h3>
                  <p className="profile-username">@{profileData.username}</p>
                  <p className="profile-role">Rol: {user.role}</p>
                </div>
              </div>
              <div className="profile-details">
                <div className="detail-item">
                  <label>Biografía</label>
                  <p>{profileData.bio}</p>
                </div>
                <div className="detail-item">
                  <label>Miembro desde</label>
                  <p>{profileData.memberSince}</p>
                </div>
                <div className="detail-item">
                  <label>Preferencia Olfativa</label>
                  <p>{profileData.preferences.scentType}</p>
                </div>
              </div>
            </div>
          </section>
        )}

        {currentView === 'admin' && canManageAdmin && (
          <section className="admin-view">
            <div className="view-header">
              <button className="btn-back" onClick={() => setCurrentView('catalog')}>← Volver al Catálogo</button>
              <h2>Panel de Administración</h2>
            </div>
            
            {adminLoading ? (
              <div style={{padding: '40px', textAlign: 'center', color: '#666'}}>
                <p>Cargando datos...</p>
              </div>
            ) : adminData ? (
              <>
                {adminData.stats?.hasErrors && adminData.stats?.errors?.length > 0 && (
                  <div className="auth-alert error" style={{margin: '20px 0'}}>
                    <strong>Errores de conexión:</strong>
                    <ul style={{margin: '10px 0 0 0', paddingLeft: '20px'}}>
                      {adminData.stats.errors.map((err, i) => (
                        <li key={i}>{err}</li>
                      ))}
                    </ul>
                  </div>
                )}
                <div className="admin-stats-grid">
                  <div className="stat-card">
                    <label>Ventas Totales</label>
                    <p className="stat-value">${(adminData.stats?.totalSales ?? 0).toFixed(2)}</p>
                  </div>
                  <div className="stat-card">
                    <label>Pedidos</label>
                    <p className="stat-value">{adminData.stats?.totalOrders ?? 0}</p>
                  </div>
                  <div className="stat-card">
                    <label>Usuarios Activos</label>
                    <p className="stat-value">{adminData.stats?.activeUsers ?? 0}</p>
                  </div>
                  <div className="stat-card">
                    <label>Top Producto</label>
                    <p className="stat-value">{adminData.stats?.topProduct ?? 'N/A'}</p>
                  </div>
                </div>

                <div className="admin-users-list">
                  <h3>Gestión de Usuarios</h3>
                  {adminData.usersError ? (
                    <div className="auth-alert error" style={{margin: '20px 0'}}>
                      {adminData.usersError}
                    </div>
                  ) : Array.isArray(adminData.users) ? (
                    adminData.users.length > 0 ? (
                      <table>
                        <thead>
                          <tr>
                            <th>Usuario</th>
                            <th>Email</th>
                            <th>Rol</th>
                          </tr>
                        </thead>
                        <tbody>
                          {adminData.users.map((u, i) => (
                            <tr key={i}>
                              <td>{u.username ?? 'N/A'}</td>
                              <td>{u.email ?? '-'}</td>
                              <td><span className={`role-badge ${(u.role ?? 'client').toLowerCase()}`}>{u.role ?? 'N/A'}</span></td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    ) : (
                      <p style={{color: '#999', padding: '20px'}}>No hay usuarios registrados.</p>
                    )
                  ) : (
                    <p style={{color: '#999', padding: '20px'}}>No se pudieron cargar los usuarios.</p>
                  )}
                </div>
              </>
            ) : null}
          </section>
        )}
      </main>

      <footer className="footer">
        <p>© 2026 PerfumerIA - Plataforma de Evaluación Full Stack</p>
      </footer>
    </div>
  );
}

export default App;
