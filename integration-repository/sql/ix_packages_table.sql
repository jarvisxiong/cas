--
-- Name: ix_packages; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--
CREATE TABLE ix_packages
(
  id                    serial NOT NULL,
  name                  character varying(128) NOT NULL,
  description           text,
  rp_data_segment_id    character varying(16),
  pmp_class             character varying(32) CHECK (pmp_class in ('PRIVATE','RIGHT_TO_FIRST_REFUSAL','PREFERRED')),
  country_ids           integer[]   NOT NULL  DEFAULT '{}',
  inventory_types       character varying(16)[]  NOT NULL  DEFAULT '{}' CHECK (inventory_types<@ '{APP,BROWSER}'),
  os_ids                smallint[]  NOT NULL  DEFAULT '{}',
  carrier_ids           bigint[]    NOT NULL  DEFAULT '{}',
  site_categories       character varying(16)[]  NOT NULL  DEFAULT '{}' CHECK (site_categories<@ '{PERFORMANCE,FAMILY_SAFE,MATURE}'),
  connection_types      character varying(16)[]  NOT NULL  DEFAULT '{}' CHECK (connection_types<@ '{WIFI,NON_WIFI}'),
  app_store_categories  smallint[]  NOT NULL  DEFAULT '{}',
  sdk_versions          character varying(16)[]  NOT NULL  DEFAULT '{}',
  lat_long_only         boolean  default FALSE,
  zip_code_only         boolean  default FALSE,
  ifa_only              boolean  default FALSE,
  site_ids              character varying(128)[]  NOT NULL  DEFAULT '{}',
  data_vendor_id        smallint,
  dmp_id                smallint,
  dmp_filter_expression text,

  zip_codes             character varying(10)[]  NOT NULL  DEFAULT '{}',
  cs_ids                integer[]  NOT NULL  DEFAULT '{}',
  min_bid               numeric(10,6),

  scheduled_tods        smallint [][]  NOT NULL  DEFAULT '{}',

  placement_ad_types    character varying(16)[]  NOT NULL  DEFAULT '{}'  CHECK (placement_ad_types<@ '{NATIVE,BANNER,VIDEO,INTERSTITIAL}'),
  placement_slot_ids    smallint[]  NOT NULL  DEFAULT '{}',

  is_active             boolean  NOT NULL  DEFAULT TRUE,
  start_date            timestamp without time zone,
  end_date              timestamp without time zone,
  last_modified         timestamp without time zone  DEFAULT now(),
  CONSTRAINT ix_packages_pkey PRIMARY KEY (id),
  CONSTRAINT ix_packages_rp_data_segment_id_key UNIQUE (rp_data_segment_id)
);
ALTER TABLE public.ix_packages OWNER TO postgres;

CREATE TABLE ix_package_deals
(
  rp_deal_id character varying(100) NOT NULL,
  package_id integer NOT NULL,
  modified_on timestamp without time zone,
  modified_by character varying(100),
  created_on timestamp without time zone,
  created_by character varying(100),
  deal_floor double precision NOT NULL DEFAULT 0.0,
  CONSTRAINT id_deals_packages_pkey PRIMARY KEY (rp_deal_id),
  CONSTRAINT id_deals_packages_package_id_fkey FOREIGN KEY (package_id)
      REFERENCES ix_packages (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);
ALTER TABLE ix_package_deals
  OWNER TO postgres;

CREATE AGGREGATE makeList (anyelement)
(
    sfunc = array_append,
    stype = anyarray,
    initcond = '{}'
);

COMMENT ON TABLE ix_package_deals IS 'Stores the deal definition for deals on IX.';
COMMENT ON COLUMN ix_package_deals.rp_deal_id IS 'Unique Deal Id';
COMMENT ON COLUMN ix_package_deals.package_id IS 'Package id corresponding to the deal';
--
-- Comment on the above table and its columns
--
COMMENT ON TABLE ix_packages IS 'Stores the package definition for deals on IX.';
COMMENT ON COLUMN ix_packages.id IS 'Unique Package Id';
COMMENT ON COLUMN ix_packages.name IS 'Name for package';
COMMENT ON COLUMN ix_packages.description IS 'Package description which should brief the package attributes';
COMMENT ON COLUMN ix_packages.rp_data_segment_id IS 'Data Segment Id of the Rubicon system';
COMMENT ON COLUMN ix_packages.pmp_class IS 'Package class';
COMMENT ON COLUMN ix_packages.country_ids IS 'Array of country Ids';
COMMENT ON COLUMN ix_packages.inventory_types IS 'Array of inventory type among {APP,BROWSER}';
COMMENT ON COLUMN ix_packages.os_ids IS 'Array of OS Ids';
COMMENT ON COLUMN ix_packages.carrier_ids IS 'Array of Carrier Ids';
COMMENT ON COLUMN ix_packages.site_categories IS 'Array of site categories among {PERFORMANCE,FAMILY_SAFE,MATURE}';
COMMENT ON COLUMN ix_packages.connection_types IS 'Array of connection Types among [WIFI,NON_WIFI]';
COMMENT ON COLUMN ix_packages.app_store_categories IS 'Array of applicable AppStore category Ids';
COMMENT ON COLUMN ix_packages.sdk_versions IS 'Array of applicable SDK versions';
COMMENT ON COLUMN ix_packages.lat_long_only IS 'Whether LATLONG is mandatory for this package';
COMMENT ON COLUMN ix_packages.zip_code_only IS 'Whether ZopCode is mandatory for this package';
COMMENT ON COLUMN ix_packages.ifa_only IS 'Whether IFA is mandatory for this package';
COMMENT ON COLUMN ix_packages.site_ids IS 'Array of applicable Site Ids.';
COMMENT ON COLUMN ix_packages.data_vendor_id IS 'Data vendor Id';
COMMENT ON COLUMN ix_packages.dmp_id IS 'DMP Id';
COMMENT ON COLUMN ix_packages.dmp_filter_expression IS 'In json array format: [[1,2],[4,5]]';
COMMENT ON COLUMN ix_packages.zip_codes IS 'Array of applicable zip codes';
COMMENT ON COLUMN ix_packages.cs_ids IS 'Array of applicable CS Ids';
COMMENT ON COLUMN ix_packages.min_bid IS 'Min bid value.';
COMMENT ON COLUMN ix_packages.scheduled_tods IS 'Scheduled time of days. Format: {DAY_OF_WEEK,START_MINUTE,END_MINUTE}=>{1,61,180}';
COMMENT ON COLUMN ix_packages.placement_ad_types IS 'Array of Ad types among {NATIVE,BANNER,VIDEO,INTERSTITIAL}';
COMMENT ON COLUMN ix_packages.placement_slot_ids IS 'Array of slot Ids';
COMMENT ON COLUMN ix_packages.is_active IS 'Whether the package is active or not';
COMMENT ON COLUMN ix_packages.start_date IS 'Start time of this package.';
COMMENT ON COLUMN ix_packages.end_date IS 'End time of this package';
COMMENT ON COLUMN ix_packages.last_modified IS 'Last modified time';
COMMENT ON COLUMN ix_packages.language_targeting_list IS 'language list';

--
-- Seed data in the ix_packages table.
--
INSERT INTO ix_packages
            (id,
            name,
            rp_data_segment_id,
            pmp_class,
            country_ids,
            site_ids,
            placement_slot_ids,
            os_ids,
            site_categories,
            lat_long_only,
            start_date,
            end_date)
     VALUES
            (1,
            'Netflix UK Tablet',
            82046,
            'RIGHT_TO_FIRST_REFUSAL',
            '{46}',
            '{ff8080812ddb8b6a012dedb8880d008f,4028cbff3977a43c013980b401ac00a4}',
            '{11,10}',
            '{3,5}',
            '{PERFORMANCE,FAMILY_SAFE}',
            false,
            '2014-11-05',
            '2014-12-15'),

            (2,
            'Netflix UK Video Interstitial (Tablet)',
            82048,
            'RIGHT_TO_FIRST_REFUSAL',
            '{46}',
            '{}',
            '{16,33}',
            '{3,5}',
            '{PERFORMANCE,FAMILY_SAFE}',
            false,
            '2014-11-05',
            '2014-12-15'),

            (3,
            'Netflix UK Video Interstitials ( Mobile)',
            82050,
            'RIGHT_TO_FIRST_REFUSAL',
            '{46}',
            '{a91586da27d04fddbab66c72d0e7e694, 051cbf944a814f638325b5bfc04b0c77}',
            '{14}',
            '{3,5}',
            '{PERFORMANCE,FAMILY_SAFE}',
            false,
            '2014-11-05',
            '2014-12-15'),

            (4,
            'UK Entertainment',
            82052,
            'RIGHT_TO_FIRST_REFUSAL',
            '{46}',
            '{60578d5face04266803b5976c1e4964a, 4028cbff36f39ce2013707b5ac9020ea, 4028cbff39009b2401396e7b1b08092f, 02dd85bef3fe4964871a08ec585505f2, 4028cb1334ef46a90135ab4a6e0920f7, f36d4382201f406a9c35680fd0889963, 4028cbff3a1c0028013a2091ae820064, 4028cbff36bbd50d0136c4aaf44100dd, 4028cbff3af511e5013af6699a100027, 55b798bd8f1c4de5b89823fbacf419bc, 217928e169b24478b5e8e3bf8032d17d, 4028cba631d63df10132262404190517, 034427c57c1a4e56b78691595ff5bbd4, bbae708831d345c9841f05dc151566d4, 4028cbff3b5f0a29013b6551960d0277, 4028cba6328f45a10132ba4d9b2502c1, 4028cbff3b5f0a29013b7423913203de, 4028cba633a085920133a145c3eb001f, 0f8015bbe1ed4d899411cb0e02dc34d6, 4028cba630724cd90131b2d5e1021425, fb894339aaf44e0c8e1330bf05c0d0c7, 4028cbff376f825001377cfeb64207e3, 4028cba6331be092013363db02d0045d, 2d1efd86ab724952874b0fdcf21435c5, 4028cbff3b77ce76013b8ceb8e2e0282, 4028cbff367d7022013688fbf4f8017a, 4028cbff3990db620139b4fa66220292, a67c640a32454e76bba7ba102a0447a1, d39c35d5dbc141df9a02d1ac6f027bf4, 4028cba630724cd901309f0bc7f10126, 4028cbff369b93f60136a153d3160149, 4028cba63479e2210134d2bb4ff9099a, 4028cbff3b77ce76013b7de50d5e001e, 80b7a453ea6241fdb8fc37ec1feaade4, 8e9adaeadc114e1cb1dc90321bc54911, 4028cba6323d864901324085740f0056, 17ed80a1691f458ea769ec3a186f95f9, 8f89cf625317457ea81079a1b34894f9, 5895ca307cb741e9b33ea58e8560b103, 4028cbff3a1c0028013a32e9a50701f0, 9678f4e51bd74c8886f6c1ee27c33724, 383243008ae34a23bdb7872d165fcfea, 5ac2fe57faf944eba20df2490e606762, 99fafeaec0f4441cb7366d20de3335e2, 85f6312609ab4ffc81c9a7db4d86339c, b75301de0da54777a0acbc6388e66a1a, 4028cba63479e2210134d2bb4ff9099a, 3ad6fff026574bab9590b11726f7ca62, 47d635c2980c4f2ca3c1121673e35a0c, 2cd9be3ab21c4e388fe37c41a40a3ed4, 817ab020cf0444fdbdd2f76e429e1514, 62d6482b37354c3096ed126e0af84afa, 3103c6adaca847d296f1100eb56ad014, c6a45d7be64e4510b2676d3c45396d2e, 16763f18f5154e30bb0a01cb4efb2e2c, 4028cbff38e2d7c00138e70aba1e006e, 571aed7e909043b88e7dc481b747c336, 4028cba630724cd901316575464c0e7c, 55b798bd8f1c4de5b89823fbacf419bc, b8ece75a6e974b2796cc8ce1632fb8b5, 4028cba630724cd9013167ce6d340e9c, 47c4d3c0d7c14be2904cfc523c95b95b, 4028cbff39009b24013942a613f50541, fbeed4bcfc05412f9ace1f750e59286b, 3fa3f2757b924a419126fdabc32f16e4, 39e7823eb68547e1ad108908dd0861b8, 6d4536d2df7649e9bbc35960059baf6a, 009a4bc640324248b519ad01d8979feb, 1361f107f0ad437d90c578c49c86c821, ced4caa266cb40258fe188c084934330, a7b214ecfbd44604b63dc04eda73e9d8}',
            '{4,15}',
            '{3,5}',
            '{PERFORMANCE,FAMILY_SAFE}',
            false,
            '2014-11-05',
            '2014-11-30'),

            (5,
            'UK Entertainment 18+ males',
            82054,
            'RIGHT_TO_FIRST_REFUSAL',
            '{46}',
            '{60578d5face04266803b5976c1e4964a, 4028cbff36f39ce2013707b5ac9020ea, 4028cbff39009b2401396e7b1b08092f, 02dd85bef3fe4964871a08ec585505f2, 4028cb1334ef46a90135ab4a6e0920f7, f36d4382201f406a9c35680fd0889963, 4028cbff3a1c0028013a2091ae820064, 4028cbff36bbd50d0136c4aaf44100dd, 4028cbff3af511e5013af6699a100027, 55b798bd8f1c4de5b89823fbacf419bc, 217928e169b24478b5e8e3bf8032d17d, 4028cba631d63df10132262404190517, 034427c57c1a4e56b78691595ff5bbd4, bbae708831d345c9841f05dc151566d4, 4028cbff3b5f0a29013b6551960d0277, 4028cba6328f45a10132ba4d9b2502c1, 4028cbff3b5f0a29013b7423913203de, 4028cba633a085920133a145c3eb001f, 0f8015bbe1ed4d899411cb0e02dc34d6, 4028cba630724cd90131b2d5e1021425, fb894339aaf44e0c8e1330bf05c0d0c7, 4028cbff376f825001377cfeb64207e3, 4028cba6331be092013363db02d0045d, 2d1efd86ab724952874b0fdcf21435c5, 4028cbff3b77ce76013b8ceb8e2e0282, 4028cbff367d7022013688fbf4f8017a, 4028cbff3990db620139b4fa66220292, a67c640a32454e76bba7ba102a0447a1, d39c35d5dbc141df9a02d1ac6f027bf4, 4028cba630724cd901309f0bc7f10126, 4028cbff369b93f60136a153d3160149, 4028cba63479e2210134d2bb4ff9099a, 4028cbff3b77ce76013b7de50d5e001e, 80b7a453ea6241fdb8fc37ec1feaade4, 8e9adaeadc114e1cb1dc90321bc54911, 4028cba6323d864901324085740f0056, 17ed80a1691f458ea769ec3a186f95f9, 8f89cf625317457ea81079a1b34894f9, 5895ca307cb741e9b33ea58e8560b103, 4028cbff3a1c0028013a32e9a50701f0, 9678f4e51bd74c8886f6c1ee27c33724, 383243008ae34a23bdb7872d165fcfea, 5ac2fe57faf944eba20df2490e606762, 99fafeaec0f4441cb7366d20de3335e2, 85f6312609ab4ffc81c9a7db4d86339c, b75301de0da54777a0acbc6388e66a1a, 4028cba63479e2210134d2bb4ff9099a, 3ad6fff026574bab9590b11726f7ca62, 47d635c2980c4f2ca3c1121673e35a0c, 2cd9be3ab21c4e388fe37c41a40a3ed4, 817ab020cf0444fdbdd2f76e429e1514, 62d6482b37354c3096ed126e0af84afa, 3103c6adaca847d296f1100eb56ad014, c6a45d7be64e4510b2676d3c45396d2e, 16763f18f5154e30bb0a01cb4efb2e2c, 4028cbff38e2d7c00138e70aba1e006e, 571aed7e909043b88e7dc481b747c336, 4028cba630724cd901316575464c0e7c, 55b798bd8f1c4de5b89823fbacf419bc, b8ece75a6e974b2796cc8ce1632fb8b5, 4028cba630724cd9013167ce6d340e9c, 47c4d3c0d7c14be2904cfc523c95b95b, 4028cbff39009b24013942a613f50541, fbeed4bcfc05412f9ace1f750e59286b, 3fa3f2757b924a419126fdabc32f16e4, 39e7823eb68547e1ad108908dd0861b8, 6d4536d2df7649e9bbc35960059baf6a, 009a4bc640324248b519ad01d8979feb, 1361f107f0ad437d90c578c49c86c821, ced4caa266cb40258fe188c084934330, a7b214ecfbd44604b63dc04eda73e9d8}',
            '{4,15}',
            '{3,5}',
            '{PERFORMANCE,FAMILY_SAFE}',
            false,
            '2014-11-05',
            '2014-11-30'),

            (6,
            'UK 18+ males',
            82056,
            'RIGHT_TO_FIRST_REFUSAL',
            '{46}',
            '{c52a99ab4f3c4e29b95306153dda9449, 46e5ea9b8ced486fb09783575629f9a4, d541d5d9ca4b42f89608aa879f92f4e0, 0f8015bbe1ed4D899411cb0e02dc34d6, 9a59c9ea1cd64385adbfdf4b973607e7, d73fd6bc53da4394af979eed4e236745, 906481e012a44bc9bcc614f53114dd9b, 3ad6fff026574bab9590b11726f7ca62, 5067168838764f2781c618c6823f2c34, 4028cbff3b77ce76013b8ceb8e2e0282, dd3375c5dbac43ddb6b6dd4e202582a5, 3c3a31761b9a4f22bac55012cc5d06bf, 98295854370440f3a295d9f43538b83d, d5c272d5cd29478b8acba9cbc3c5e8be, 968dac3a732042f79008c882e3afe137, 18ca7b79fcb046eeb80f934b07529fbc, e5c498af501445fbbffe66675140da13, 79c1234269464bce91f04418ed95099e, 3fa3f2757b924a419126fdabc32f16e4, 734dd5be9c1c4a04be79925ea748c7fe, 7315a675ca034537bd051960e9591d73, c924229c44b94836b72dd06194178a6a, 23a9ca1518954351bea33d84570581cb, 3066717ebc114fb093153af9d7b31660, 93d24955d1f54259a439689e7dbf978f, b02ce6e735984b7dbf219c21c50f9a5b, 48b02cafa5b142b0bdbae592200d3330, c97b06734ff0449cadc4065ceab35b80, 4028cbff3a1c0028013a2091ae820064, ff8080812d54a4d8012d5ea4a0fa0036, 77be37017a19446ca43592e1358c8f21, 577f798f84c3491d9153191cb6eddf5d, 3ca6572dee9c48d089549ff615247ec7, c5f7e4bad3d14c77986c288bfb0c31ee, 4028cbff3a6eaf57013a90d4d1a10342, ca1ff7ae68184100a840ad5a65ddbd12, 64d724419b274de9a7ed523e05fcf405, 79678ba362774f518e9f4089d6b6eb7f, 4028cb1333eded0b01342518392808a0, 3286bb5de1374426ada896201320f792, c7312fb1cbdd44a889e1c1e7fe3e0d43, 4028cb962c56b261012c66549909008b, 4028cbff3aab0518013ab2560117007e, 83a82b79ecdf428b9b8af161253c7252, c0bb2461d7b34b97a9f576049c99d334, f36d4382201f406a9c35680fd0889963, 0f97564dee6b4e1bb5176964352f0343, 98f2c888335c4d29b2205eeb72abd00a, 4028cbff36f39ce201375e75fc20338f, 4028cba631d63df101322624c8190518, 4028cba635ca529a0135d03d25ea0096, 8f0249ef6ad04952a64dd9092a0ef92a, 7bc219bf4e5e4e848f2b19a92f108d17, 4028cbff38989ad10138a492fc9f03b9, 4028cbff39009b2401394a5b98d0061f, 4028cbff3b93b240013ba11f64ba00d5, d0f24385c16b4fc5bad769899fe23115, 4028cbff3b187e27013b4b1610f00888, 29cc49cbf96941aabfa0ea1725f4052e, fb894339aaf44e0c8e1330bf05c0d0c7, 56476a48a1c248a7847056bfd5f20991, 4028cba63016e1a80130223c123a00ac, 27ddb10487e04a02a90fd4e475079fba, 4028cbff39009b24013942a613f50541, 58cd93d397fe43a18b13dd819ca8f36a, 3decd24fd6b74a098d9e6c018606c8f0, 4028cba6331be092013344e268360258, 4028cbff36bbd50d0136c4aaf44100dd, 4028cb133479e4180134e7e236880956, 034427c57c1a4e56b78691595ff5bbd4, 300d7876076e441fb4eb901b43d60a0d, 4028cbff3b93b240013bda6b437004df, 06cef8a21c9b4c7b92a30ef169b722e8, dad3b1b63e6442c3a608b2137b853181, 55999decef264afa8f949a82683740a0, 70c57747e54244b3b0419dbb5375ac12, 02dd85bef3fe4964871a08ec585505f2, 3103c6adaca847d296f1100eb56ad014, 4028cba630724cd9013104ec3c400761, 4028cbff3990db62013997bb69d200a2, 11c73f05f82548cf9736469045288a12, 4028cbff38e2d7c00138e66821060051, ff8080812e898efa012e91d1aeb50037, b16298882cea45e7b87775d0cd5784be, 62d6482b37354c3096ed126e0af84afa, 361aa042dc544753a5e38089a0f6d9a3, b1c9ae5e3ce04eef9e384576af423e68, e0c883df7e22427ca5c5301814e3053d, 4028cba6323d864901324085740f0056, 57896e0f317b4841a1e3d02bc5c481ba, da442c8e9cd444868d1c4c7828541063, 5a854d8f1f474e0bb342e0268ef52945, 772a21a515f74f96aae592fc62fdd2b0, 4028cbff379738bf0137e4e23ee20edb, 4028cba6328f45a10132ba4d9b2502c1, 4028cba630724cd90131b2d5e1021425, 009a4bc640324248b519ad01d8979feb, 4028cbff3a6eaf57013a9f49f04c04f7, 662002142a37426d8d34405182cc9fa3, c3902fdb0a4c4a64a914ebaca632d55a, 4028cb13338bfc5d01338cdc4fd90013, d8a337cbbfce48b9a2b7ef5efa010836, b668267e3b8e494482825b77872d226d, 072bd873df0c48ee90dcf28ed2f6805e, 069932cdb8c24aad905cf1ab101e76b1, 71227374e8094e678eec5ec7f7c7e955, 72618d118dad4a59857280876f6cb6c6, de04a50a74ad449fb37e6942cf2f22a4, 4028cbff36f39ce2013707b5ac9020ea, d83d8ee2cbaf4b839a7a228ee82b4a21, bee535cddecd421584e7eba49c570365, 4028cba630724cd9013188210cbe1075, 4028cbff369b93f60136a153d3160149, 28fec6b030654b4d8d802aa7cce5ea4f, 75f1fd31cdbf4a4991ca5dccaea4693a, 6112dcc6846145f38c5ce4371980a53f, 0d39762658c641388b643988885528b7, ae1445026e9a4e0da3f58b97bdcb6b33, 50c27e87370c4c95a57545d1903ee125, d776fbad4e9542ed8be2a0113b5eb283, 4028cbff376f8250013774e2ae8100d0, 4028cbff376f8250013774e1b74500ce, 4028cbff36350dab0136595202e4032f, 4028cbff3b5f0a29013b70da17dc039f, 4028cbff3af511e5013af6699a100027}',
            '{4,15}',
            '{3,5}',
            '{PERFORMANCE,FAMILY_SAFE}',
            false,
            '2014-11-05',
            '2014-11-30'),

            (7,
            'Netflix UK Click to Video (Banners)',
            82058,
            'RIGHT_TO_FIRST_REFUSAL',
            '{46}',
            '{}',
            '{4,15}',
            '{3,5}',
            '{PERFORMANCE,FAMILY_SAFE}',
            false,
            '2014-11-05',
            '2014-12-15'),

            (8,
            'UK Sports 18+ males',
            82060,
            'RIGHT_TO_FIRST_REFUSAL',
            '{46}',
            '{71fc85cdde2340b5896dc25173ba3368, 4028cbff3a6eaf57013a95a668f7038f, 4028cbff3b5f0a29013b65e3b2fd0298, 4028cbff376f825001377cfeb64207e3, 4028cbff379738bf0137a7f45a7c0539, 5ac2fe57faf944eba20df2490e606762, 4028cbff3b5f0a29013b6551960d0277, fb894339aaf44e0c8e1330bf05c0d0c7, 4028cbff3a1c0028013a3f8a0be30293, 4028cba630724cd90131b2d5e1021425, 4028cba6328f45a10132ba4d9b2502c1, 4028cba633a085920133a145c3eb001f, 817ab020cf0444fdbdd2f76e429e1514, 4ceb44b7f9e444859d340f2c7c3402a3, 8f89cf625317457ea81079a1b34894f9, 8e9adaeadc114e1cb1dc90321bc54911, c2894b1abeb1435da7e0381faeda2df6, 366eee2a5945421eac72556980ec7e9f, 3ad6fff026574bab9590b11726f7ca62, 4028cbff3b77ce76013b7de50d5e001e, 4028cba630724cd901309f0bc7f10126, 9678f4e51bd74c8886f6c1ee27c33724, 4028cbff38e2d7c00138e70aba1e006e, 16763f18f5154e30bb0a01cb4efb2e2c, 4028cb13338bfc5d01338cdc4fd90013, 4028cba630724cd901316575464c0e7c, 4028cbff369b93f60136a153d3160149, 4028cba6323d864901324085740f0056, 80b7a453ea6241fdb8fc37ec1feaade4, 99fafeaec0f4441cb7366d20de3335e2, 4028cbff39009b24013942a613f50541, b75301de0da54777a0acbc6388e66a1a, 17ed80a1691f458ea769ec3a186f95f9, 97d960fb43314e08b76bc3f4f2ac65ab, 4028cbff3b93b240013bbc6f4022039c, 4028cbff3af511e5013b038c17930182, 4028cbff3990db620139b9b9eea30305, 0116b1d223ac4dce9383fbb0d27966c7, e635a090521244e6a62a050d33b1b8dd, 2868059ea13642228745df82e19f0137, 4028cb13331be40d013382f2ef2008bc, 62d6482b37354c3096ed126e0af84afa, 4028cba6328f45a10132e4427eb50596, a6dfefdf98b748689637645707d4c8d3, 79c1234269464bce91f04418ed95099e, fbeed4bcfc05412f9ace1f750e59286b, 4028cbff3b93b240013bb24df0ea0252, 5d79ef4990b14ed98d15cfc96f5edf99, 0d39762658c641388b643988885528b7, 19565ed4291a44529202d5585025d681, cfa36e615f0948b99c9385d0b5988f05, 6c34b81c67cb4821b19c6f19e1fbc961, 4028cbff3a6eaf57013a888b02a3017b, 4028cba6331be092013344e268360258, da442c8e9cd444868d1c4c7828541063, 4028cbff3b187e27013b472089ff0840}',
            '{4,15}',
            '{3,5}',
            '{PERFORMANCE,FAMILY_SAFE}',
            false,
            '2014-11-05',
            '2014-11-30'),

            (9,
            'UK Shoppers 18+ males',
            82062,
            'RIGHT_TO_FIRST_REFUSAL',
            '{46}',
            '{4028cbff3a1c0028013a2091ae820064, 0f8015bbe1ed4d899411cb0e02dc34d6, 034427c57c1a4e56b78691595ff5bbd4, 60578d5face04266803b5976c1e4964a, 4028cbff3a1c0028013a32e9a50701f0, 02dd85bef3fe4964871a08ec585505f2, 4028cb1334ef46a90135ab4a6e0920f7, 71fc85cdde2340b5896dc25173ba3368, 4028cbff3a1c0028013a3f8a0be30293, 4028cbff3af511e5013af6699a100027, 4028cbff376f825001377cfeb64207e3, 4028cbff3b5f0a29013b65e3b2fd0298, 5ac2fe57faf944eba20df2490e606762, 4028cbff38e2d7c00138e70aba1e006e, 4028cba630724cd901316575464c0e7c, 4028cba6323d864901324085740f0056, 4028cbff3b5f0a29013b6551960d0277, 72618d118dad4a59857280876f6cb6c6, 5d79ef4990b14ed98d15cfc96f5edf99, 4028cba630724cd901309f0bc7f10126, c2894b1abeb1435da7e0381faeda2df6, 16763f18f5154e30bb0a01cb4efb2e2c, 4028cbff39bf65120139e52f9a4b02fd, 4028cba6328f45a10132ba4d9b2502c1, 4028cbff36f39ce2013707b5ac9020ea, 4028cba630724cd90131b2d5e1021425, 4028cbff39009b24013942a613f50541, 8e9adaeadc114e1cb1dc90321bc54911, 217928e169b24478b5e8e3bf8032d17d, 3ad6fff026574bab9590b11726f7ca62, 9678f4e51bd74c8886f6c1ee27c33724, 99fafeaec0f4441cb7366d20de3335e2, e635a090521244e6a62a050d33b1b8dd, 55b798bd8f1c4de5b89823fbacf419bc, 55b798bd8f1c4de5b89823fbacf419bc, ff8080812d54a4d8012d5ea4a0fa0036, 4028cbff3b77ce76013b7de50d5e001e, b0dfb332d35a429b9c9c93bdb8d07b4d, 4028cbff369b93f60136a153d3160149, 4028cb13331be40d013382f2ef2008bc, 73325a6cfdef4d8c83a3d4c95cb53e24, 4028cbff3af511e5013b187403f902aa, 4028cbff39009b24013942a81d850543, cfa36e615f0948b99c9385d0b5988f05, 4028cbff39009b24013942a8ed720544, a0b95b62a63342d4bf8803fe10d601cd, f36d4382201f406a9c35680fd0889963, 19565ed4291a44529202d5585025d681, fb894339aaf44e0c8e1330bf05c0d0c7, 8f89cf625317457ea81079a1b34894f9, 4028cbff3a6eaf57013a888b02a3017b, 4028cba630724cd9013188210cbe1075, 4028cb13338bfc5d01338cdc4fd90013, 817ab020cf0444fdbdd2f76e429e1514, 80b7a453ea6241fdb8fc37ec1feaade4, d84ffea2792446918fa6ec593d0073b4, 4028cbff36bbd50d0136c4aaf44100dd, c924229c44b94836b72dd06194178a6a, 4028cba633a085920133a145c3eb001f, 97d960fb43314e08b76bc3f4f2ac65ab, 4028cba630724cd90131b2d5e1021425, 4028cbff39009b24013942a613f50541, 8e9adaeadc114e1cb1dc90321bc54911, 217928e169b24478b5e8e3bf8032d17d, 3ad6fff026574bab9590b11726f7ca62, 9678f4e51bd74c8886f6c1ee27c33724, 99fafeaec0f4441cb7366d20de3335e2, e635a090521244e6a62a050d33b1b8dd, 55b798bd8f1c4de5b89823fbacf419bc, 55b798bd8f1c4de5b89823fbacf419bc, ff8080812d54a4d8012d5ea4a0fa0036, 4028cbff3b77ce76013b7de50d5e001e, b0dfb332d35a429b9c9c93bdb8d07b4d, 4028cbff369b93f60136a153d3160149, 4028cb13331be40d013382f2ef2008bc, 73325a6cfdef4d8c83a3d4c95cb53e24, 4028cbff3af511e5013b187403f902aa, 4028cbff39009b24013942a81d850543, cfa36e615f0948b99c9385d0b5988f05, 4028cbff39009b24013942a8ed720544, a0b95b62a63342d4bf8803fe10d601cd, f36d4382201f406a9c35680fd0889963, 19565ed4291a44529202d5585025d681, fb894339aaf44e0c8e1330bf05c0d0c7, 8f89cf625317457ea81079a1b34894f9, 4028cbff3a6eaf57013a888b02a3017b, 4028cba630724cd9013188210cbe1075, 4028cb13338bfc5d01338cdc4fd90013, 817ab020cf0444fdbdd2f76e429e1514, 80b7a453ea6241fdb8fc37ec1feaade4, d84ffea2792446918fa6ec593d0073b4, 4028cbff36bbd50d0136c4aaf44100dd, c924229c44b94836b72dd06194178a6a, 4028cba633a085920133a145c3eb001f, 97d960fb43314e08b76bc3f4f2ac65ab}',
            '{4,15}',
            '{3,5}',
            '{PERFORMANCE,FAMILY_SAFE}',
            false,
            '2014-11-05',
            '2014-11-30');
