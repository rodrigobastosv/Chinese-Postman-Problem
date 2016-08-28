      SUBROUTINE CPP(IKD,IK,N,M,KST,NB,KOST,INDEX,TOP,BASIS,MEM,KA,KB,
     1               SM,TMA,TMB,Y1,Y2,DPLUS,DMINUS,KURS,EPS,IW,NDUP,
     2               ARQ,IF1,IF2)
C *** *****************************************************************
C     *                                                               *
C     *                   CHINESE POSTMAN PROBLEM                     *
C     *                                                               *
C *** *****************************************************************
C     *                                                               *
C     * 1. CALL:                                                      *
C     *    CALL CPP (N,M,KST,KOST,INDEX,TOP,NEXT,BASIS,MEM,KA,KB,     *
C     *              SM,TMA,TMB,Y1,Y2,DPLUS,DMINUS,KURS,IW,NDUP,      *
C     *                                                               *
C     * 2. LANGUAGE:                                                  *
C     *    FORTRAN IV                                                 *
C     *                                                               *
C     * 3. METHOD:                                                    *
C     *    LABELING METHOD OF EDMONDS AND JOHNSON                     *
C     *                                                               *
C     * 4. PARAMETER:                                                 *
C     *    INPUT:                                                     *
C     *       N         NUMBER OF NODES                               *
C     *       M         NUMBER OF EDGES                               *
C     *       TOP       SUFFICIELTLY LARGE INTEGER                    *
C     *       EPS       MACHINE ACCURACY                              *
C     *       KURS      STARTNOUD OF THE POSTMAN TOUR                 *
C     *       NB(I)     CUMULATED LIST OF NEIGHBOURS                  *
C     *       KOST(I)   COST OF EDGE INDICATED BY ENTRY NB(I)         *
C     *                 (I=1,...,2*M)                                 *
C     *       INDEX(I)  INDEX OF THE FIRST NEIGHBOUR OF NODE I        *
C     *                 IN NEIGHBOURLIST NB (I=1,...,N+1)             *
C     *                                                               *
C     *    OUTPUT:                                                    *
C     *       KST       TOTAL COAST OF THE DUPLICATED EDGES           *
C     *       KOSTEB    TOTAL COAST OF THE OPTIMAL POSTAM TOUR        *
C     *       KOST(I)   CUMULATED NEXT NODE LIST OF THE               *
C     *                 OPTIMAL POSTMAN TOUR STARTING AT NODE KURS    *
C     *       INDEX(I)  INDEX OF THE FIRST NEIGHBOUR OF NODE I        *
C     *                 IN THE NEXT NODE NEXT                         *
C     *       KB(I)     INDEX OF THE LAST NEIGHBOUR OF NODE I         *
C     *                 IN THE NEXT NODE LIST NEXT                    *
C     *       NDUP      NUMBER OF DUPLICATED EDGES                    *
C     *                                                               *
C     *    INTEGER ARRAYS OF LENGHT N:                                *
C     *       BASIS(I),MEN(I),KA(I),KB(I),SM(I),TMA(I),TMB(I)         *
C     *    INTEGER ARRAY OF LENGHT N+1:                               *
C     *       INDEX(I)                                                *
C     *    REAL ARRAYS OF LENGHT N:                                   *
C     *       Y1(I),Y2(I),DPLUS(I),DMINUS(I)                          *
C     *                                                               *
C     * 5. EXTERNALS:                                                 *
C     *    SUBROUTINE PRUEF1                                          *
C     *    SUBROUTINE PRUEF2                                          *
C     *    SUBROUTINE SCHR                                            *
C     *    SUBROUTINE ERW                                             *
C     *    SUBROUTINE KASU                                            *
C     *    SUBROUTINE TOUR                                            *
C     *                                                               *
C     * 6. COMMENTS:                                                  *
C     *                                                               *
C     *    THE PROGRAMM EXPECTS THE GRAPH TO BE CONNECTED             *
C     *                                                               *
C     *    THE LIST OF DUPLICATED EDGES IS PRINTED OUT                *
C     *    AT THE END OF THIS ROUTINE                                 *
C     *                                                               *
C     * 7. AUTHOR:                                                    *
C     *    W. PUETZ                                                   *
C     *                                                               *
C *** *****************************************************************
      IMPLICIT INTEGER*4 (B,I-M)
      INTEGER*4  NB(IK),KOST(IK),INDEX(IK),TOP
      INTEGER*4  BASIS(IK),MEM(IK),KA(IK),KB(IK)
      INTEGER*4  SM(IK),TMA(IK),TMB(IK)
      REAL*8     Y1(IK),Y2(IK),DPLUS(IK),DMINUS(IK)
      CHARACTER NOMERUA*18,ARQ(3)*12
C - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
      NP = N+1
      NM = -NP
      DO 1001 KN = 1,N
         DMINUS(KN) = TOP
 1001 CONTINUE
C - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
C
C
C *** INITIAL LABELING
C
      DO 1000 KN = 1,N
         BASIS(KN) = KN
         MEM  (KN) = KN
         SM   (KN) = NM
         TMA  (KN) =  0
         KB   (KN) = KN
         Y1   (KN) = 0.
         Y2   (KN) = 0.
         I         = INDEX(KN)
         II        = INDEX(KN+1)
         B         = II-I
         B1        = B/2
         B1        = B1*2
         IF (B.EQ.B1) GOTO 1000
         II        = II-1
         SM   (KN) = 0
         DPLUS(KN) = 0.
      DO 1020  KN1 = I,II
         KN2         = NB(KN1)
         D1          = FLOAT(KOST(KN1))
         IF (DMINUS(KN2).LE.D1) GOTO 1020
         KA(KN2)     = KN
         DMINUS(KN2) = D1
 1020  CONTINUE
 1000  CONTINUE
C - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
C *** EXAMINATION OF THE LABELING AND
C     DECISION OF NEXT STEP
C
 2001 D         = TOP
      DO 2000 B1 = 1,N
         IF (BASIS(B1).NE.B1) GOTO 2000
         D1   = DMINUS(B1)
         IF (SM(B1).LT.0)     GOTO 2005
         D1   = .5*(D1+DPLUS(B1))
         IF (D.LT.D1)         GOTO 2000
         D    = D1
         BEST = B1
                              GOTO 2000
 2005    IF (TMA(B1).GT.0)    D1 = D1+Y1(B1)
         IF (D.LE.D1)         GOTO 2000
         D    = D1
         BEST = B1
 2000 CONTINUE
      FLTOP     = FLOAT(TOP)/2.
      IF (D.GE.FLTOP)     GOTO 9993
      IF (SM(BEST).LT.0)  GOTO 2010
      KN1       = KA(BEST)
      KN2       = KB(BEST)
      B         = BASIS(KN1)
      B1        = BEST
      B2        = B
 2020 TMA(B1)   = B2
      KN        = SM(B1)
      IF (KN.EQ.0)        GOTO 2030
      B2        = BASIS(KN)
      KN        = TMA(B2)
      B1        = BASIS(KN)
                         GOTO 2020
 2030 BASE      = B1
      B1        = B
      B2        = BEST
 2040 IF (TMA(B1).GT.0)  GOTO 2050
      TMA(B1)   = B2
      KN        = SM(B1)
      IF (KN.EQ.0)       GOTO 7000
      B2        = BASIS(KN)
      KN        = TMA(B2)
      B1        = BASIS(KN)
                         GOTO 2040
 2050 IF (B1.EQ.BASE)    GOTO 4000
      B3        = TMA (BASE)
      TMA(BASE) = 0
      KN3       = -SM(B3)
      BASE      = BASIS(KN3)
                         GOTO 2050
 2010 IF (TMA(BEST).LE.0) GOTO 3000
      B         = MEM(BEST)
      IF (B.EQ.BEST)     GOTO 6000
      KN        = TMA(B)
      IF (KN.GT.0)       GOTO 5000
              GO TO 6000
C - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
C *** GROWING AND ALTERNATING TREE
C
 3000 KN            = -SM(BEST)
      IF (KN.GT.N)       GOTO 3010
      TMA(BEST)     = KA(BEST)
      TMB(BEST)     = KB(BEST)
      B             = BASIS(KN)
      SM(B)         = -SM(B)
      DMINUS(B)     = TOP
      DPLUS(B)      = D
      CALL PRUEF1  (IK,N,NB,KOST,INDEX,TOP,BASIS,MEM,KA,KB,
     *              SM,Y1,Y2,DPLUS,DMINUS,B)
                                       GOTO 2001
 3010 KN1           = KA(BEST)
      B             = BASIS(KN1)
      SM(BEST)      = SM(B)
      DMINUS(BEST)  = TOP
      DPLUS(BEST)   = D
      SM(B)         = KN1
      Y1(B)         = Y1(B)+D-DPLUS(B)
      KN4           = B
      CALL SCHR (IK,N,BASIS,MEM,Y1,Y2,BEST,KN4)
      KB(B)         = KN4
      MEM(BEST)     = B
      MEM(KN4)      = BEST
      CALL PRUEF1  (IK,N,NB,KOST,INDEX,TOP,BASIS,MEM,KA,KB,
     *              SM,Y1,Y2,DPLUS,DMINUS,BEST)
                                GOTO 2001
C - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
C *** SHRINKING OF A BLOSSOM
C
 4000 Y             = Y1(BASE)+D-DPLUS(BASE)
      Y1(BASE)      = 0.
      KN4           = BASE
 4010 Y2(KN4)       = Y2(KN4)+Y
      KN4           = MEM(KN4)
      IF (KN4.NE.BASE)   GOTO 4010
      KN3           = MEM(BASE)
      IF (BASE.NE.B)     GOTO 4020
 4030 B             = BEST
      B2            = TMA(BASE)
 4020 MEM(KN4)      = B2
      KN            = -SM(B2)
      SM(B2)        = KN
      Y1(B2)        = Y1(B2)+DMINUS(B2)-D
      KN4           = B2
      CALL SCHR (IK,N,BASIS,MEM,Y1,Y2,BASE,KN4)
      KB(B2)         = KN4
      B1            = BASIS(KN)
      MEM(KN4)      = B1
      Y1(B1)        = Y1(B1)+D-DPLUS(B1)
      KN4           = B1
      CALL SCHR (IK,N,BASIS,MEM,Y1,Y2,BASE,KN4)
      KB(B1)        = KN4
      IF (B.EQ.B1)       GOTO 4040
      B2            = TMA(B1)
      TMA(B1)       = TMB(B2)
      TMB(B1)       = TMA(B2)
                         GOTO 4020
 4040 IF (B.EQ.BEST)     GOTO 4050
      TMA(B)        = KN2
      TMB(B)        = KN1
      IF (BASE.NE.BEST)  GOTO 4030
                         GOTO 4060
 4050 TMA(BEST)     = KN1
      TMB(BEST)     = KN2
 4060 MEM(KN4)      = KN3
      B             = MEM(BASE)
      KA(B)         = KN3
      DPLUS(B)      = Y
      TMA(BASE)     = 0
      DMINUS(BASE)  = TOP
      DPLUS(BASE)   = D
      CALL PRUEF1 (IK,N,NB,KOST,INDEX,TOP,BASIS,MEM,KA,KB,
     *             SM,Y1,Y2,DPLUS,DMINUS,BASE)
                         GO TO 2001
C - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
C *** EXPANDING A BLOSSOM
C
 5000 KN3        = KA(B)
      B1         = B
      CALL ERW (IK,N,BASIS,MEM,KB,Y1,Y2,B1,KN3)
      Y          = DPLUS(B)
      Y1(BEST)   = Y
      MEM(BEST)  = KN3
 5010 Y2(KN3)    = Y2(KN3) -Y
      IF (KN3.EQ.BEST)   GOTO 5020
      KN3        = MEM(KN3)
                         GOTO 5010
 5020 KN1        = -SM(BEST)
      B1         = BASIS(KN1)
      KN2        = SM(B1)
      BASE       = BASIS(KN2)
      IF(BASE.EQ.BEST)   GOTO 5100
      B2         = BASE
 5030 KN         = TMA(B2)
      B1         = BASIS(KN)
      IF (B1.EQ.BEST)    GOTO 5040
      KN         = SM(B1)
      B2         = BASIS(KN)
                         GOTO 5030
 5040 TMA(BASE)  = TMA(BEST)
      TMA(BEST)  = TMB(B2)
      TMB(BASE)  = TMB(BEST)
      TMB(BEST)  = KN
      KN3        = SM(BASE)
      B          = BASIS(KN3)
      KN4        = SM(B)
      SM(BASE)   = -KN1
      B1         = B
 5050 KN1        = TMA(B1)
      KN2        = TMB(B1)
      TMA(B1)    = KN4
      TMB(B1)    = KN3
      SM(B1)     = KN1
      B2         = BASIS(KN1)
      KN3        = SM(B2)
      SM(B2)     = KN2
      IF (B2.EQ.BEST)    GOTO 5100
      B1         = BASIS(KN3)
      KN4        = SM(B1)
      TMA(B2)    = KN3
      TMB(B2)    = KN4
                         GOTO 5050
 5100 KN2        = TMB(BASE)
      B1         = BASIS(KN2)
      DMINUS(B1) = D
      IF (B1.EQ.BASE)    GOTO 5200
      KN1        = TMA(B1)
      B          = BASIS(KN1)
      TMA(B1)    = TMA(BASE)
      TMB(B1)    = KN2
 5110 KN         = SM(B1)
      SM(B1)     = -KN
      B2         = BASIS(KN)
      KN         = TMA(B2)
      TMA(B2)    = -KN
      DMINUS(B2) = TOP
      DPLUS(B2)  = D
      B1         = BASIS(KN)
      DMINUS(B1) = D
      CALL PRUEF1(IK,N,NB,KOST,INDEX,TOP,BASIS,MEM,KA,KB,
     *            SM,Y1,Y2,DPLUS,DMINUS,B2)
      IF (B1.NE.BASE)    GOTO 5110
      TMA(BASE)  = TMB(B2)
      TMB(BASE)  = KN
      IF (B.EQ.BASE)     GOTO 2001
 5200 B2         = B
 5210 KN         = SM(B2)
      SM(B2)     = -KN
      B1         = BASIS(KN)
      TMA(B2)    = -B1
      KN         = TMA(B1)
      SM(B1)     = -SM(B1)
      B2         = BASIS(KN)
      TMA(B1)    = -B2
      IF (B2.NE.BASE)    GOTO 5210
 5220 B1         = -TMA(B)
      CALL PRUEF2 (IK,N,NB,KOST,INDEX,TOP,BASIS,MEM,KA,KB,
     *             SM,Y1,Y2,DPLUS,DMINUS,B,B)
      B          = -TMA(B1)
      CALL PRUEF2 (IK,N,NB,KOST,INDEX,TOP,BASIS,MEM,KA,KB,
     *             SM,Y1,Y2,DPLUS,DMINUS,B1,B1)
      IF (B.NE.BASE)     GOTO 5220
                         GOTO 2001
C - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
C *** MODIFICATION OF A BLOSSOM
C
 6000 DMINUS(BEST) = TOP
      DPLUS(BEST)  = D
      I            = 1
      Y1(BEST)     = 0.
      KN           = -SM(BEST)
      B            = BASIS(KN)
      KN1          = SM(B)
      IF (KN1.NE.BEST)   GOTO 6040
      I            = 2
      SM(B)        = KN
      KN3          = MEM(BEST)
      MEM(BEST)    = B
      Y1(B)        = Y1(B)+D-DPLUS(B)
      KN4          = B
      CALL SCHR (IK,N,BASIS,MEM,Y1,Y2,BEST,KN4)
      KB(B)        = KN4
      MEM(KN4)     = KN3
      KN1          = TMB(BEST)
      IF (KN1.NE.BEST)   GOTO 6040
 6110 KN           = TMA(BEST)
      B            = BASIS(KN)
      SM(BEST)     = SM(B)
      SM(B)        = KN
      TMA(BEST)    = 0
      KN3          = MEM(BEST)
      MEM(BEST)    = B
      Y1(B)        = Y1(B)+D-DPLUS(B)
      KN4          = B
      CALL SCHR (IK,N,BASIS,MEM,Y1,Y2,BEST,KN4)
      KB(B)        = KN4
      MEM(KN4)     = KN3
      CALL PRUEF1 (IK,N,NB,KOST,INDEX,TOP,BASIS,MEM,KA,KB,
     *             SM,Y1,Y2,DPLUS,DMINUS,BEST)
                         GOTO 2001
 6040 KN2          = BEST
      B1           = MEM(BEST)
 6080 KN3          = B1
      KN4          = KB(B1)
 6070 IF (KN3.EQ.KN1)    GOTO 6050
      IF (KN3.EQ.KN4)    GOTO 6060
      KN3          = MEM(KN3)
                         GOTO 6070
 6060 B1           = MEM(KN4)
      KN2          = KN4
                         GOTO 6080
 6050 KN3          = MEM(KN4)
      MEM(KN2)     = KN3
      CALL ERW (IK,N,BASIS,MEM,KB,Y1,Y2,B1,KN3)
      DMINUS(B1)   = D
      IF (I.EQ.2)        GOTO 6170
      I            = 2
      TMA(B1)      = BEST
      TMB(B1)      = SM(B1)
      SM(B1)       = -KN
      KN1          = TMB(BEST)
      IF (KN1.EQ.BEST)   GOTO 6110
      IF (BASIS(KN1).EQ.BEST) GOTO 6040
      TMA(B1)      = TMA(BEST)
      TMB(B1)      = KN1
      TMA(BEST)    = 0
      B1           = MEM(BEST)
      IF (B1.NE.BEST)    GOTO 6130
      SM(BEST)     = NM
      BBEST        = BEST
      CALL PRUEF2 (IK,N,NB,KOST,INDEX,TOP,BASIS,MEM,KA,KB,
     *             SM,Y1,Y2,DPLUS,DMINUS,BBEST,BEST)
                         GOTO 2001
 6130 KN4          = KB(B1)
      KN3          = MEM(KN4)
      MEM(BEST)    = KN3
      CALL ERW (IK,N,BASIS,MEM,KB,Y1,Y2,B1,KN3)
      SM(BEST)     = -SM(B1)
      SM(B1)       = -BEST
      BBB1         = B1
      CALL PRUEF2 (IK,N,NB,KOST,INDEX,TOP,BASIS,MEM,KA,KB,
     *             SM,Y1,Y2,DPLUS,DMINUS,BBB1,B1)
      BBEST        = BEST
      CALL PRUEF2 (IK,N,NB,KOST,INDEX,TOP,BASIS,MEM,KA,KB,
     *             SM,Y1,Y2,DPLUS,DMINUS,BBEST,BEST)
                         GOTO 2001
 6170 TMA(B1)      = TMA(BEST)
      TMB(B1)      = KN1
      TMA(BEST)    = 0
      SM(BEST)     = SM(B1)
      SM(B1)       = -BEST
      CALL PRUEF1 (IK,N,NB,KOST,INDEX,TOP,BASIS,MEM,KA,KB,
     *             SM,Y1,Y2,DPLUS,DMINUS,BEST)
                         GOTO 2001
C - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
C *** AUGMENTATION
C
 7000 II           = 0
      DO 7010 BB   = 1,N
      IF (BASIS(BB).NE.BB) GOTO 7010
      KN3          = SM(BB)
      IF (KN3.LT.0)      GOTO 7020
      IF (KN3.EQ.0)      II = II+1
      D1           = D-DPLUS(BB)
      DPLUS(BB)    = 0.
      Y1(BB)       = Y1(BB)+D1
      SM(BB)       = -KN3
                         GOTO 7010
 7020 KN3          = TMA(BB)
      IF (KN3.LE.0)      GOTO 7010
      D1           = DMINUS(BB)-D
      Y1(BB)       = Y1(BB)+D1
      TMA(BB)      = -KN3
 7010 CONTINUE
 7030 IF (B1.EQ.B)       GOTO 7040
      B2           = TMA(B1)
      TMA(B1)      = 0
      KN3          = -TMA(B2)
      KN4          = TMB(B2)
      SM(B1)       = -KN4
      KN           = -SM(B2)
      SM(B2)       = -KN3
      B1           = BASIS(KN)
                         GOTO 7030
 7040 IF (B.EQ.BEST)     GOTO 7050
      TMA(B)       = 0
      SM(B)        = -KN2
      SM(BEST)     = -KN1
      B            = BEST
      B1           = BASE
                         GOTO 7030
 7050 TMA(BEST)    = 0
      KN           = 1
      IF (II.LE.2)      GOTO 8000
      CALL PRUEF2 (IK,N,NB,KOST,INDEX,TOP,BASIS,MEM,KA,KB,
     *             SM,Y1,Y2,DPLUS,DMINUS,KN,N)
                         GOTO 2001
C - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
C *** GENERATION OF THE ORIGINAL GRAPH
C     BY EXPANDING ALL PSEUDDONODES
C
 8000 KST              = 0
      DO 8010 BB = 1,N
         IF (BASIS(BB).NE.BB) GOTO 8010
         KN1        = -SM(BB)
         IF (KN1.EQ.NP)     GOTO 8010
         IF (KN1.LT.0)      GOTO 8010
         B          = BASIS(KN1)
         KN2        = -SM(B)
      CALL KASU (IK,NB,KOST,INDEX,TOP,KN1,KN2,KANT)
         D          = -FLOAT (KOST(KANT))
         D          = D+Y1(BB)+Y1(B)
         D          = D+Y2(KN1)+Y2(KN2)
      IF(ABS(D).GT.EPS)  GOTO 9995
         KST        = KST+KOST(KANT)
         SM(B)      = KN1
         SM(BB)     = KN2
 8010 CONTINUE
      DO 8030 BB = 1,N
 8040    IF (MEM(BB).EQ.BB) GOTO 8030
         BASE       = BASIS(BB)
         B          = MEM(BASE)
         KN1        = TMA(B)
         IF (KN1.GT.0)   GOTO 8050
         KN2        = SM(BASE)
         MEM(BASE)  = BASE
         Y          = Y2(BASE)
         Y1(BASE)   = 0.
         Y2(BASE)   = 0.
 8060    KN4        = KB(B)
         KN3        = MEM(KN4)
      CALL ERW (IK,N,BASIS,MEM,KB,Y1,Y2,B,KN3)
         B2         = BASIS(KN2)
         IF (B2.EQ.B)    GOTO 8070
         KN1        = SM(B)
      CALL KASU (IK,NB,KOST,INDEX,TOP,BASE,KN1,KANT)
         D          = -FLOAT(KOST(KANT))
         KST        = KST+KOST(KANT)
         D          = D+Y2(KN1)+Y1(B)+Y
      IF(ABS(D).GT.EPS)  GOTO 9995
                         GOTO 8080
 8070    SM(B)         = KN2
 8080    B             = KN3
         IF (B.NE.BASE)  GOTO 8060
                         GOTO 8040
 8050    KN3           = KA(B)
         B1            = B
      CALL ERW (IK,N,BASIS,MEM,KB,Y1,Y2,B1,KN3)
         MEM(BASE)     = KN3
C
         Y             = DPLUS(B)
         Y1(BASE)      = Y
 8105    Y2(KN3)       = Y2(KN3)-Y
         IF (KN3.EQ.BASE) GOTO 8115
         KN3           = MEM(KN3)
                         GOTO 8105
 8115    KN1           = SM(BASE)
         B1            = BASIS(KN1)
         IF (B1.EQ.BASE) GOTO 8110
         B             = TMA(B1)
         B             = BASIS(B)
         KN3           = SM(B1)
         SM(B1)        = KN1
 8120    B2            = BASIS(KN3)
         KN1           = TMA(B2)
         KN2           = TMB(B2)
         B1            = BASIS(KN1)
      CALL KASU (IK,NB,KOST,INDEX,TOP,KN1,KN2,KANT)
         D             = -FLOAT(KOST(KANT))
         KST           = KST+KOST(KANT)
         D             = D+Y1(B1)+Y1(B2)
         D             = D+Y2(KN1)+Y2(KN2)
      IF (ABS(D).GT.EPS) GOTO 9995
         SM(B2)        = KN2
         KN3           = SM(B1)
         SM(B1)        = KN1
         IF (B1.NE.BASE) GOTO 8120
 8130    IF (B.EQ.BASE)  GOTO 8040
 8110    KN3           = SM(B)
         B1            = BASIS(KN3)
         KN4           = SM(B1)
      CALL KASU (IK,NB,KOST,INDEX,TOP,KN3,KN4,KANT)
         KST           = KST+KOST(KANT)
         D             = -FLOAT(KOST(KANT))
         D             = D+Y1(B)+Y1(B1)
         D             = D+Y2(KN3)+Y2(KN4)
      IF (ABS(D).GT.EPS)  GOTO 9995
         SM(B)         = KN4
         SM(B1)        = KN3
         KN2           = TMA(B1)
         B             = BASIS(KN2)
                         GOTO 8130
 8030 CONTINUE
C - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
C *** PRINTING THE LIST OF DUPLICATED EDGES
C
      WRITE(IW,9991)
      NDUP = 0
      I       = INDEX(2)
	OPEN(UNIT=33,FILE='XIMBIL',STATUS='UNKNOWN')
	write(33,31031)
31031 format('  Lista dos Arcos Duplicados:',/)
      DO 8993 KN = 2,N
      II      = INDEX(KN+1)-1
      DO 8983 KN2 = I,II
      KN3     = NB(KN2)
      IF (KN3.GT.0)      GOTO 8983
      KN3     = -KN3
      IF (KN3.GT.KN)     GOTO 8983
      WRITE(33,199) KN,KN3
 199  FORMAT(2I5)
      NDUP = NDUP + 1
 8983 CONTINUE
      I       = II+1
 8993 CONTINUE
C
C       IDENTIFICA SENTIDO DE TRAFEGO NOS ARCOS DUPLICADOS
C
	REWIND(33)
630	READ(33,199,END=631)KN,KN3
      OPEN(UNIT=IF1,FILE=ARQ(1),STATUS='UNKNOWN')
      READ(IF1,14) IFLAG
14    FORMAT(I5)
      READ(IF1,140)N1,M1
140   FORMAT(2I5)
        IQ=N1/20
        IR=N1-20*IQ
        IF(IR.NE.0)GOTO 600
        N11=IQ
        GOTO 601
600     N11=IQ+1
601     DO 602 L=1,N11
           READ(IF1,603)LINHA
603        FORMAT(I2)
602     CONTINUE
        DO 604 L=1,IKD
           READ(IF1,30)IANF,IEND,IKOST,ICODRUA,ITRANS
30         FORMAT(I5,1X,I5,2X,2I5,I3)
           IF(IANF.EQ.KN3.AND.IEND.EQ.KN)GOTO 605
           GOTO 604
605          OPEN(UNIT=IF2,FILE=ARQ(2),STATUS='UNKNOWN')           
606          READ(IF2,607,END=608)ICOD,NOMERUA             
607          FORMAT(I5,A18)
             IF(ICODRUA.NE.ICOD)GOTO 606             
             IF(ITRANS.EQ.2)GOTO 609
             WRITE(IW,610)KN,KN3,NOMERUA
610          FORMAT(10X,'(',I4,',',I4,')',' SENTIDO PROIBIDO ',A18)
             GOTO 617
609          WRITE(IW,611)KN,KN3,NOMERUA
611          FORMAT(10X,'(',I4, ',',I4,')',' SENTIDO PERMITIDO ', A18)
             GOTO 617
604     CONTINUE 
C           
C
617     CLOSE(IF1)
608     CLOSE(IF2)
 	  GOTO 630
631	  CLOSE(33)
C - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
C *** CONSTRUCTING THE NEXT NODE LIST OF THE OPTIMAL POSTMAN TOUR
C     STARTING AT NODE KURS
C
      CALL TOUR(IK,N,NB,KOST,INDEX,TOP,KB,BASIS,KURS)
      GOTO 9997
 9991 FORMAT(///,10X,26HLISTA DE ARCOS DUPLICADOS:,/)
 9992 FORMAT(10X,1H(,I4,1H,,I4,1H),A18)
 9993 WRITE(IW,9994)
 9994 FORMAT(/////,10X,18HCONDICAO DE ERRO :,/,
     *10X,33HO ARQUIVO DE ENTRADA NAO ESTA BOM,/,
     *10X,23HOU TOP E MUITO PEQUENO.)
      STOP
 9995 WRITE(IW,9996)
 9996 FORMAT(/////,10X,43HAS CONDICOES DE OPTIMALIDADE FORAM VIOLADAS
     *,/,10X,42HPOR FAVOR VERIFIQUE SEU ARQUIVO DE ENTRADA)
      STOP
 9997 RETURN
      END
