import React, { useState } from 'react';
import { ChevronDown, ChevronUp, HelpCircle, ShoppingBag, Truck, Shield, CreditCard, RotateCcw, PhoneCall, User } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import Header from '../../components/Header/Header';
import Footer from '../../components/Footer/Footer';
import SEO from '../../components/SEO/SEO';
import './FAQPage.css';

const FAQPage = () => {
  const { t } = useTranslation();
  const [activeSection, setActiveSection] = useState('general');
  const [openItems, setOpenItems] = useState({});

  const toggleItem = (itemId) => {
    setOpenItems(prev => ({
      ...prev,
      [itemId]: !prev[itemId]
    }));
  };

  const faqSections = {
    general: {
      title: t('pages.faq.sections.general'),
      icon: <HelpCircle size={20} />,
      questions: [
        {
          id: 'g1',
          question: t('pages.faq.questions.g1.question'),
          answer: t('pages.faq.questions.g1.answer')
        },
        {
          id: 'g2',
          question: t('pages.faq.questions.g2.question'),
          answer: t('pages.faq.questions.g2.answer')
        },
        {
          id: 'g3',
          question: t('pages.faq.questions.g3.question'),
          answer: t('pages.faq.questions.g3.answer')
        },
        {
          id: 'g4',
          question: t('pages.faq.questions.g4.question'),
          answer: t('pages.faq.questions.g4.answer')
        }
      ]
    },
    shopping: {
      title: t('pages.faq.sections.shopping'),
      icon: <ShoppingBag size={20} />,
      questions: [
        {
          id: 's1',
          question: t('pages.faq.questions.s1.question'),
          answer: t('pages.faq.questions.s1.answer')
        },
        {
          id: 's2',
          question: t('pages.faq.questions.s2.question'),
          answer: t('pages.faq.questions.s2.answer')
        },
        {
          id: 's3',
          question: t('pages.faq.questions.s3.question'),
          answer: t('pages.faq.questions.s3.answer')
        },
        {
          id: 's4',
          question: t('pages.faq.questions.s4.question'),
          answer: t('pages.faq.questions.s4.answer')
        }
      ]
    },
    payment: {
      title: t('pages.faq.sections.payment'),
      icon: <CreditCard size={20} />,
      questions: [
        {
          id: 'p1',
          question: t('pages.faq.questions.p1.question'),
          answer: t('pages.faq.questions.p1.answer')
        },
        {
          id: 'p2',
          question: t('pages.faq.questions.p2.question'),
          answer: t('pages.faq.questions.p2.answer')
        },
        {
          id: 'p3',
          question: t('pages.faq.questions.p3.question'),
          answer: t('pages.faq.questions.p3.answer')
        },
        {
          id: 'p4',
          question: t('pages.faq.questions.p4.question'),
          answer: t('pages.faq.questions.p4.answer')
        }
      ]
    },
    shipping: {
      title: t('pages.faq.sections.shipping'),
      icon: <Truck size={20} />,
      questions: [
        {
          id: 'sh1',
          question: t('pages.faq.questions.sh1.question'),
          answer: t('pages.faq.questions.sh1.answer')
        },
        {
          id: 'sh2',
          question: t('pages.faq.questions.sh2.question'),
          answer: t('pages.faq.questions.sh2.answer')
        },
        {
          id: 'sh3',
          question: t('pages.faq.questions.sh3.question'),
          answer: t('pages.faq.questions.sh3.answer')
        },
        {
          id: 'sh4',
          question: t('pages.faq.questions.sh4.question'),
          answer: t('pages.faq.questions.sh4.answer')
        }
      ]
    },
    returns: {
      title: t('pages.faq.sections.returns'),
      icon: <RotateCcw size={20} />,
      questions: [
        {
          id: 'r1',
          question: t('pages.faq.questions.r1.question'),
          answer: t('pages.faq.questions.r1.answer')
        },
        {
          id: 'r2',
          question: t('pages.faq.questions.r2.question'),
          answer: t('pages.faq.questions.r2.answer')
        },
        {
          id: 'r3',
          question: t('pages.faq.questions.r3.question'),
          answer: t('pages.faq.questions.r3.answer')
        },
        {
          id: 'r4',
          question: t('pages.faq.questions.r4.question'),
          answer: t('pages.faq.questions.r4.answer')
        }
      ]
    },
    sellers: {
      title: t('pages.faq.sections.sellers'),
      icon: <User size={20} />,
      questions: [
        {
          id: 'se1',
          question: t('pages.faq.questions.se1.question'),
          answer: t('pages.faq.questions.se1.answer')
        },
        {
          id: 'se2',
          question: t('pages.faq.questions.se2.question'),
          answer: t('pages.faq.questions.se2.answer')
        },
        {
          id: 'se3',
          question: t('pages.faq.questions.se3.question'),
          answer: t('pages.faq.questions.se3.answer')
        },
        {
          id: 'se4',
          question: t('pages.faq.questions.se4.question'),
          answer: t('pages.faq.questions.se4.answer')
        }
      ]
    },
    support: {
      title: t('pages.faq.sections.support'),
      icon: <PhoneCall size={20} />,
      questions: [
        {
          id: 'su1',
          question: t('pages.faq.questions.su1.question'),
          answer: t('pages.faq.questions.su1.answer')
        },
        {
          id: 'su2',
          question: t('pages.faq.questions.su2.question'),
          answer: t('pages.faq.questions.su2.answer')
        },
        {
          id: 'su3',
          question: t('pages.faq.questions.su3.question'),
          answer: t('pages.faq.questions.su3.answer')
        },
        {
          id: 'su4',
          question: t('pages.faq.questions.su4.question'),
          answer: t('pages.faq.questions.su4.answer')
        }
      ]
    },
    warranty: {
      title: t('pages.faq.sections.warranty'),
      icon: <Shield size={20} />,
      questions: [
        {
          id: 'w1',
          question: t('pages.faq.questions.w1.question'),
          answer: t('pages.faq.questions.w1.answer')
        },
        {
          id: 'w2',
          question: t('pages.faq.questions.w2.question'),
          answer: t('pages.faq.questions.w2.answer')
        },
        {
          id: 'w3',
          question: t('pages.faq.questions.w3.question'),
          answer: t('pages.faq.questions.w3.answer')
        },
        {
          id: 'w4',
          question: t('pages.faq.questions.w4.question'),
          answer: t('pages.faq.questions.w4.answer')
        }
      ]
    }
  };

  const FAQItem = ({ question, answer, isOpen, onToggle }) => (
    <div className="faq-item">
      <button className="faq-question" onClick={onToggle}>
        <span>{question}</span>
        {isOpen ? <ChevronUp size={20} /> : <ChevronDown size={20} />}
      </button>
      {isOpen && (
        <div className="faq-answer">
          <p>{answer}</p>
        </div>
      )}
    </div>
  );

  return (
    <div className="faq-page">
      <SEO
        title={t('pages.faq.title')}
        description={t('pages.faq.subtitle')}
        keywords="الأسئلة الشائعة, FAQ, مساعدة, دعم العملاء, GamersStation, أسئلة وأجوبة"
        url="https://gamers-station.com/faq"
      />
      <Header />
      
      <div className="faq-hero">
        <div className="hero-content">
          <h1>{t('pages.faq.title')}</h1>
          <p>{t('pages.faq.subtitle')}</p>
        </div>
      </div>

      <div className="faq-container">
        <aside className="faq-sidebar">
          <h3>{t('pages.faq.sectionsTitle')}</h3>
          <nav className="faq-nav">
            {Object.entries(faqSections).map(([key, section]) => (
              <button
                key={key}
                className={`faq-nav-item ${activeSection === key ? 'active' : ''}`}
                onClick={() => setActiveSection(key)}
              >
                {section.icon}
                <span>{section.title}</span>
              </button>
            ))}
          </nav>
        </aside>

        <main className="faq-content">
          <div className="section-header">
            <h2>{faqSections[activeSection].title}</h2>
          </div>
          
          <div className="faq-list">
            {faqSections[activeSection].questions.map(item => (
              <FAQItem
                key={item.id}
                question={item.question}
                answer={item.answer}
                isOpen={openItems[item.id]}
                onToggle={() => toggleItem(item.id)}
              />
            ))}
          </div>
        </main>
      </div>

      <section className="contact-section">
        <div className="contact-container">
          <h2>{t('pages.faq.noAnswer')}</h2>
          <p>{t('pages.faq.supportReady')}</p>
          <div className="contact-options">
            <div className="contact-card">
              <Shield size={32} />
              <h3>{t('pages.contact.email')}</h3>
              <p>contact@thegamersstation.com</p>
              <small>{t('pages.faq.replyWithin24h')}</small>
            </div>
            <div className="contact-card">
              <HelpCircle size={32} />
              <h3>{t('pages.faq.liveChat')}</h3>
              <p>{t('pages.faq.instantChat')}</p>
              <small>{t('pages.faq.available247')}</small>
            </div>
          </div>
        </div>
      </section>
      
      <Footer />
    </div>
  );
};

export default FAQPage;